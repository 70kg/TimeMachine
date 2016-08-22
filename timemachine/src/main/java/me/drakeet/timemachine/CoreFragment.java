package me.drakeet.timemachine;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import java.util.List;
import me.drakeet.multitype.MultiTypeAdapter;
import me.drakeet.timemachine.message.OutTextContent;
import me.drakeet.timemachine.message.TextContent;
import me.drakeet.timemachine.scroller.SnapperSmoothScroller;

import static java.util.Objects.requireNonNull;

/**
 * @author drakeet
 */
public class CoreFragment extends Fragment implements CoreContract.View, View.OnClickListener {

    private ImageButton leftAction;
    private ImageButton rightAction;
    private EditText input;

    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private MultiTypeAdapter adapter;
    private List<Message> messages;

    private OnRecyclerItemClickListener itemClickListener;
    private GestureDetector gestureDetector;

    private CoreContract.Presenter presenter;
    private CoreContract.Delegate delegate;
    private MessageObserver messageObserver;

    private RecyclerView.SmoothScroller smoothScroller;
    private MessageFactory messageFactory;


    public CoreFragment() {
    }


    public static CoreFragment newInstance() {
        CoreFragment fragment = new CoreFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override public void setDelegate(@NonNull final CoreContract.Delegate delegate) {
        this.delegate = requireNonNull(delegate);
    }


    @Override public void setService(@NonNull final CoreContract.Service service) {
        initPresenter(requireNonNull(service));
    }


    @Override public void setMessageObserver(@NonNull MessageObserver observer) {
        this.messageObserver = requireNonNull(observer);
    }


    /**
     * To notify the implementor should init the presenter
     *
     * @return for misuse
     */
    @NonNull @Override
    public CoreContract.Presenter initPresenter(@NonNull final CoreContract.Service service) {
        requireNonNull(service);
        CoreContract.Presenter presenter = new MessagePresenter(this, service);
        this.changePresenter(presenter);
        return presenter;
    }


    /**
     * For change presenter by someone
     *
     * @param presenter new presenter
     */
    @Override public void changePresenter(@NonNull final CoreContract.Presenter presenter) {
        this.presenter = requireNonNull(presenter);
        delegate.setPresenter(presenter);
    }


    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        messages = delegate.provideInitialMessages();
        adapter = new MultiTypeAdapter(messages);
        messageFactory = new MessageFactory.Builder()
            .setFromUserId(TimeKey.userId)
            .setToUserId("")
            .build();
    }


    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_core, container, false);
        setupRecyclerView(rootView);
        leftAction = (ImageButton) rootView.findViewById(R.id.left_action);
        input = (EditText) rootView.findViewById(R.id.input);
        rightAction = (ImageButton) rootView.findViewById(R.id.right_action);
        leftAction.setOnClickListener(this);
        rightAction.setOnClickListener(this);
        return rootView;
    }


    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


    private void setupRecyclerView(@NonNull final View rootView) {
        requireNonNull(rootView);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.list);
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        itemClickListener = new OnRecyclerItemClickListener(getContext()) {
            @Override void onItemClick(View view, int position) {
                if (messageObserver != null) {
                    messageObserver.onMessageClick(messages.get(position));
                }
            }


            @Override void onItemLongClick(View view, int position) {
                if (messageObserver != null) {
                    messageObserver.onMessageLongClick(messages.get(position));
                }
            }
        };
        recyclerView.addOnItemTouchListener(itemClickListener);
        gestureDetector = new GestureDetector(getContext(),
            new GestureDetector.SimpleOnGestureListener() {
                @Override public boolean onSingleTapUp(MotionEvent e) {
                    if (Keyboards.isShown(input)) {
                        Keyboards.hide(input);
                        return true;
                    }
                    return false;
                }
            });
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
        smoothScroller = new SnapperSmoothScroller(getContext())
            .setMillisecondsPerInchSearchingTarget(200f);
    }


    @Override public void onNewIn(@NonNull final Message message) {
        requireNonNull(message);
        addMessage(message);
        if (messageObserver != null) {
            messageObserver.onNewIn(message);
        }
    }


    @Override public void onNewOut(@NonNull final Message message) {
        requireNonNull(message);
        addMessage(message);
        if (messageObserver != null) {
            messageObserver.onNewOut(message);
        }
    }


    private void addMessage(@NonNull final Message message) {
        requireNonNull(message);
        int size = messages.size();
        messages.add(message);
        adapter.notifyItemInserted(size);
        attemptSmoothScrollToBottom();
    }


    @Override public void setInputText(@NonNull CharSequence text) {
        requireNonNull(text);
        input.setText(text);
        input.setSelection(text.length());
    }


    @Override public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.left_action) {
            delegate.onLeftActionClick();
        } else if (id == R.id.right_action && !TextUtils.isEmpty(input.getText().toString())) {
            TextContent content = new OutTextContent(input.getText().toString());
            Message message = messageFactory.newMessage(content);
            if (!delegate.onRightActionClick() && !presenter.onInterceptNewOut(message)) {
                input.setText("");
                presenter.addNewOut(message);
            }
            delegate.onRightActionClick();
        }
    }


    private void attemptSmoothScrollToBottom() {
        int last = messages.size() - 1;
        if (layoutManager.findLastVisibleItemPosition() >= last - 2) {
            smoothScroller.setTargetPosition(last);
            recyclerView.getLayoutManager().startSmoothScroll(smoothScroller);
        }
    }


    @Override public void onDataSetChanged() {
        adapter.notifyDataSetChanged();
        recyclerView.scrollToPosition(messages.size() - 1);
    }


    @Override public void onClear() {
        messages.clear();
        onDataSetChanged();
    }


    @Override public void onDestroy() {
        super.onDestroy();
        recyclerView.removeOnItemTouchListener(itemClickListener);
    }
}
