package me.drakeet.timemachine;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
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

/**
 * @author drakeet
 */
public class CoreFragment extends Fragment
    implements CoreContract.View, View.OnClickListener, CoreHelper.CoreFragment {

    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private MessageAdapter adapter;
    private ImageButton leftAction;
    private ImageButton rightAction;
    private EditText input;
    private View inputField;

    private List<Message> messages;

    CoreContract.Delegate delegate;
    CoreContract.Service service;
    OnRecyclerItemClickListener itemClickListener;
    GestureDetector gestureDetector;
    CoreHelper coreHelper;
    MessagePresenter dispatcher;


    public CoreFragment() {
    }


    public static CoreFragment newInstance() {
        CoreFragment fragment = new CoreFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    private void parseArguments() {
        Bundle bundle = this.getArguments();
    }


    @Override public void setDelegate(CoreContract.Delegate delegate) {
        this.delegate = delegate;
    }


    @Override public void setService(CoreContract.Service service) {
        this.service = service;
        dispatcher = new MessagePresenter(this, service);
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
        inputField = rootView.findViewById(R.id.input_field);
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
        recyclerView.setItemAnimator(new DefaultItemAnimator());
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
    }


    @Override public void onNewIn(Message message) {
        addMessage(message);
        delegate.onNewIn(message);
    }


    @Override public void onNewOut(Message message) {
        addMessage(message);
        delegate.onNewOut(message);
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
                dispatcher.addNewOut(message);
                offsetIfInBottom();
            }
            delegate.onRightActionClick();
        }
    }


    private void offsetIfInBottom() {
        if (layoutManager.findLastVisibleItemPosition() == messages.size() - 2) {
            recyclerView.smoothScrollToPosition(Integer.MAX_VALUE);
        }
    }


    @Override public void onDestroy() {
        super.onDestroy();
        recyclerView.removeOnItemTouchListener(itemClickListener);
    }


    private void addMessage(Message message) {
        int _size = messages.size();
        messages.add(message);
        adapter.notifyItemInserted(_size);
    }


    @Override public void notifyDataSetChanged() {
        adapter.notifyDataSetChanged();
    }


    @Override public void clear() {
        messages.clear();
        notifyDataSetChanged();
    }
}
