package com.espressif.iot.ui.group;

import java.util.List;

import com.espressif.iot.R;
import com.espressif.iot.group.IEspGroup;
import com.espressif.iot.group.IEspGroup.Type;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class EspGroupEditActivity extends Activity implements OnClickListener {

    private IEspUser mUser;

    private RecyclerView mRecyclerView;
    private Type[] mTypes;
    private Type mSelectedType = Type.COMMON;

    private EditText mNameET;

    private int mNumber;
    private EditText mNumberET;
    private TextView mNumberAddBtn;
    private TextView mNumberMinusBtn;

    private Button mCancelBtn;
    private Button mOKBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.group_edit_activity);

        mUser = BEspUser.getBuilder().getInstance();

        mTypes = Type.values();
        mRecyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mRecyclerView.setAdapter(new Adapter(this));

        mNameET = (EditText)findViewById(R.id.name_edit);
        mNameET.addTextChangedListener(mNameTextWather);

        mNumber = 1;
        mNumberET = (EditText)findViewById(R.id.number_edit);
        mNumberET.setText("" + mNumber);
        mNumberAddBtn = (TextView)findViewById(R.id.number_add_btn);
        mNumberAddBtn.setOnClickListener(this);
        mNumberMinusBtn = (TextView)findViewById(R.id.number_minus_btn);
        mNumberMinusBtn.setOnClickListener(this);

        mCancelBtn = (Button)findViewById(R.id.cancel_btn);
        mCancelBtn.setOnClickListener(this);
        mOKBtn = (Button)findViewById(R.id.confirm_btn);
        mOKBtn.setOnClickListener(this);
    }

    private class VH extends RecyclerView.ViewHolder {
        ImageView icon;
        Type type;

        public VH(View itemView) {
            super(itemView);

            icon = (ImageView)itemView.findViewById(R.id.icon);
            icon.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mSelectedType = type;
                    mRecyclerView.getAdapter().notifyDataSetChanged();
                }
            });
        }
    }

    private class Adapter extends RecyclerView.Adapter<VH> {
        private Activity mActivity;

        public Adapter(Activity activity) {
            mActivity = activity;
        }

        @Override
        public int getItemCount() {
            return mTypes.length;
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            Type type = mTypes[position];
            holder.type = type;
            holder.icon.setImageResource(type.getIconRes());
            if (type == mSelectedType) {
                holder.icon.setBackgroundResource(R.drawable.group_type_bg_selected);
            } else {
                holder.icon.setBackgroundResource(R.drawable.group_type_bg);
            }
        }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = View.inflate(mActivity, R.layout.group_type_item, null);
            VH vh = new VH(view);

            return vh;
        }

    }

    @Override
    public void onClick(View v) {
        if (v == mNumberAddBtn) {
            mNumber++;
            mNumberET.setText("" + mNumber);
        } else if (v == mNumberMinusBtn) {
            if (mNumber > 1) {
                mNumber--;
                mNumberET.setText("" + mNumber);
            }
        } else if (v == mCancelBtn) {
            finish();
        } else if (v == mOKBtn) {
            createGroup(mNameET.getText().toString(), mNumber);
            setResult(RESULT_OK);
            finish();
        }
    }

    private TextWatcher mNameTextWather = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            mOKBtn.setEnabled(!TextUtils.isEmpty(s));
        }
    };

    private void createGroup(String groupName, int num) {
        List<IEspGroup> userGroupList = mUser.getGroupList();
        int suffixes = 2;
        for (int i = 0; i < num; i++) {
            String newName = groupName;
            if (i > 0) {
                newName += suffixes;
            }

            while (true) {
                boolean hasSameName = false;
                for (IEspGroup group : userGroupList) {
                    if (group.getName().equals(newName)) {
                        hasSameName = true;
                        break;
                    }
                }

                if (hasSameName) {
                    newName = groupName + suffixes++;
                    continue;
                } else {
                    suffixes++;
                    break;
                }
            }

            mUser.doActionGroupCreate(newName, mSelectedType.ordinal());
        }
    }
}
