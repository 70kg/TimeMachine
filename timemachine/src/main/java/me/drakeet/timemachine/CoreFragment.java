package me.drakeet.timemachine;

import android.os.Bundle;
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
import me.drakeet.timemachine.scroller.SnapperSmoothScroller;

/**
 * @author drakeet
 */
public class CoreFragment extends Fragment
    implements CoreContract.View, View.OnClickListener, CoreHelper.CoreFragment {

    private static final String TAG = CoreFragment.class.getSimpleName();
    private ImageButton leftAction;
    private ImageButton rightAction;
    private EditText input;

    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private MessageAdapter adapter;
    private List<Message> messages;

    private OnRecyclerItemClickListener itemClickListener;
    private GestureDetector gestureDetector;

    private CoreContract.Presenter presenter;
    private CoreContract.Delegate delegate;
    private CoreHelper coreHelper;

    private RecyclerView.SmoothScroller smoothScroller;


    public CoreFragment() {
    }


    public static CoreFragment newInstance() {
        CoreFragment fragment = new CoreFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override public void setDelegate(CoreContract.Delegate delegate) {
        this.delegate = delegate;
    }


    @Override public void setService(CoreContract.Service service) {
        initPresenter(service);
    }


    /**
     * To notify the implementor should init the presenter
     *
     * @return for misuse
     */
    @Override public CoreContract.Presenter initPresenter(CoreContract.Service service) {
        CoreContract.Presenter presenter = new MessagePresenter(this, service);
        this.changePresenter(presenter);
        return presenter;
    }


    /**
     * For change presenter by someone
     *
     * @param presenter new presenter
     */
    @Override public void changePresenter(CoreContract.Presenter presenter) {
        this.presenter = presenter;
        delegate.setPresenter(presenter);
    }


    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        messages = delegate.provideInitialMessages();
        adapter = new MessageAdapter(messages);
        coreHelper = CoreHelper.attach(this);
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


    private void setupRecyclerView(View rootView) {
        recyclerView = (RecyclerView) rootView.findViewById(R.id.list);
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        itemClickListener = new OnRecyclerItemClickListener(getContext()) {
            @Override void onItemClick(View view, int position) {
                delegate.onMessageClick(messages.get(position));
            }


            @Override void onItemLongClick(View view, int position) {
                delegate.onMessageLongClick(messages.get(position));
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
            .setMillisecondsPerInchSearchingTarget(100f);
    }


    @Override public void onNewIn(Message message) {
        addMessage(message);
        delegate.onNewIn(message);
    }


    @Override public void onNewOut(Message message) {
        addMessage(message);
        delegate.onNewOut(message);
    }


    private void addMessage(Message message) {
        int _size = messages.size();
        messages.add(message);
        adapter.notifyItemInserted(_size);
        smoothScrollToBottom();
    }


    @Override public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.left_action) {
            delegate.onLeftActionClick();
        } else if (id == R.id.right_action && !TextUtils.isEmpty(input.getText().toString())) {
            Message message = new SimpleMessage.Builder()
                .setContent(input.getText().toString())
                .setFromUserId(TimeKey.userId)
                .setToUserId("")
                .setCreatedAt(new Now())
                .build();
            if (!delegate.onRightActionClick()) {
                input.setText("");
                presenter.addNewOut(message);
            }
            delegate.onRightActionClick();
        }
    }


    private void smoothScrollToBottom() {
        int last = messages.size() - 1;
        if (layoutManager.findLastVisibleItemPosition() == last - 2) {
            smoothScroller.setTargetPosition(last);
            recyclerView.getLayoutManager().startSmoothScroll(smoothScroller);
        }
    }


    @Override public void onDataSetChanged() {
        adapter.notifyDataSetChanged();
        // TODO: 16/6/30
        recyclerView.scrollToPosition(messages.size() - 1);
    }


    @Override public void onClean() {
        messages.clear();
        onDataSetChanged();
    }


    @Override public void onDestroy() {
        super.onDestroy();
        recyclerView.removeOnItemTouchListener(itemClickListener);
    }
}
