package everypony.tabun.mail.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.*;

/**
 * Загрузщик изображений.
 */
public class ImageLoader {

    private final Map<String, WeakReference<BitmapWithOptions>> ram_cache;
    private final List<Map.Entry<String, ImageHandler>> onLoaded;
    private final Map<String, ImageLoading> loading_tasks;
    private final HttpClient client;

    public synchronized void addTask(String address, ImageHandler handler) {

        // Проверяем, есть ли у нас в памяти этот битмап?
        if (ram_cache.containsKey(address)) {
            WeakReference<BitmapWithOptions> possibly_cached = ram_cache.get(address);
            BitmapWithOptions bitmap;

            // Если битмап уже есть в кэше, то пихаем его сразу в handler.
            if ((bitmap = possibly_cached.get()) != null) {
                handler.handleBitmap(bitmap.bitmap, bitmap.options);
                return;
            }

        }

        // Тут мы поняли, что в ram-кэше совсем ничего не осталось, и нужно пилить загрузку.
        onLoaded.add(new AbstractMap.SimpleEntry<>(address, handler));

        // Если в потоках нет потока загрузки этого изображения, то добавляем.
        if (!loading_tasks.containsKey(address)) {
            ImageLoading loading = new ImageLoading(address);
            loading_tasks.put(address, loading);
            loading.execute();

        }

    }

    protected synchronized void publish(String str, Bitmap bitmap, BitmapFactory.Options opt) {
        Iterator<Map.Entry<String, ImageHandler>> iterator = onLoaded.iterator();
        Map.Entry<String, ImageHandler> e;

        while (iterator.hasNext() && (e = iterator.next()) != null) {
            if (e.getKey().equals(str)) {
                e.getValue().handleBitmap(bitmap, opt);
                iterator.remove();
            }
        }

    }

    protected synchronized void removeTask(String path) {
        loading_tasks.remove(path);
    }

    public ImageLoader() {
        ram_cache = new HashMap<>();
        onLoaded = new ArrayList<>();
        loading_tasks = new HashMap<>();
        client = new DefaultHttpClient();
    }


    public static interface ImageHandler {
        public void handleBitmap(Bitmap bitmap, BitmapFactory.Options options);
    }

    public static class BitmapWithOptions {
        BitmapFactory.Options options;
        Bitmap bitmap;
    }


    /**
     * Задание загрузки
     */
    public class ImageLoading extends AsyncTask<Void, Void, Void> {
        BitmapFactory.Options opt;
        int retry_count = 3;
        final String path;
        Bitmap bitmap;

        public ImageLoading(String path) {
            this.path = path;
        }

        @Override protected Void doInBackground(Void... none) {

            while (bitmap == null && retry_count > 0)
                try {
                    retry_count--;

                    HttpResponse response = client.execute(
                            new HttpGet(URI.create(path))
                    );

                    opt = new BitmapFactory.Options();
                    InputStream content = response.getEntity().getContent();
                    bitmap = BitmapFactory.decodeStream(content, null, opt);

                } catch (IOException e) {
                    Au.w(this, e);
                } catch (OutOfMemoryError e) {
                    retry_count = 0;
                }

            return null;
        }


        @Override protected void onPostExecute(Void none) {
            publish(path, bitmap, opt);
            removeTask(path);
        }

    }

}
