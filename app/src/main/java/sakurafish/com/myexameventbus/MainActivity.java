package sakurafish.com.myexameventbus;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        // Subscriberは再利用できないので、Observerを使う
        private Observer<? super Object> mReceiverEventObserver = new Observer<Object>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(Object value) {
                if (value instanceof ReceiverEvent) {
                    ReceiverEvent event = (ReceiverEvent) value;
                    ((TextView) getView().findViewById(R.id.TextView)).setText(event.message);
                    Toast.makeText(getActivity(), event.message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        };

        private Observer<? super Object> mThreadEventObserver = new Observer<Object>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(Object value) {
                if (value instanceof ThreadEvent) {
                    ThreadEvent event = (ThreadEvent) value;
                    ((TextView) getView().findViewById(R.id.TextView)).setText(event.message);
                    Toast.makeText(getActivity(), event.message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        };

        @Override
        public void onStart() {
            super.onStart();

            // イベント監視をスタートする
            RxBus.instanceOf().getObservable()
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(mReceiverEventObserver);

            RxBus.instanceOf().getObservable()
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(mThreadEventObserver);
        }

        @Override
        public void onStop() {
            super.onStop();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_main, container, false);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            getView().findViewById(R.id.button).setOnClickListener(v -> {
                // バックグラウンド処理を開始します
                runThread();
            });
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            mReceiverEventObserver.onComplete();
            mThreadEventObserver.onComplete();
        }

        private void runThread() {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    RxBus.instanceOf().post(new ThreadEvent("スレッドからの通知"));
                }
            }, 3000); //3sec遅延
        }
    }
}
