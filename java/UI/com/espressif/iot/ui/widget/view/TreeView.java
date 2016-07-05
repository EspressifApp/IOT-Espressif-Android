package com.espressif.iot.ui.widget.view;

import java.util.ArrayList;
import java.util.List;

import com.espressif.iot.R;
import com.espressif.iot.adt.tree.IEspDeviceTreeElement;
import com.espressif.iot.adt.tree.IEspTreeComponent;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.model.adt.tree.EspTreeComposite;
import com.espressif.iot.model.adt.tree.EspTreeLeaf;
import com.espressif.iot.type.device.IEspDeviceState;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class TreeView extends ListView implements ListView.OnItemClickListener, ListView.OnItemLongClickListener
{
    private List<IEspTreeComponent<IEspDeviceTreeElement>> mTreeElements =
        new ArrayList<IEspTreeComponent<IEspDeviceTreeElement>>();
    
    private List<IEspDeviceTreeElement> mCurrentElements = new ArrayList<IEspDeviceTreeElement>();
    
    // get tree component by id
    private IEspTreeComponent<IEspDeviceTreeElement> __getTreeComponentById(
        List<IEspTreeComponent<IEspDeviceTreeElement>> treeList, String id)
    {
        for (IEspTreeComponent<IEspDeviceTreeElement> treeInList : treeList)
        {
            if (treeInList.getEspElement().getId().equals(id))
            {
                return treeInList;
            }
        }
        return null;
    }
    
    // build the tree
    private void __buildTree(List<IEspDeviceTreeElement> elementList)
    {
        List<IEspTreeComponent<IEspDeviceTreeElement>> treeComponentList =
            new ArrayList<IEspTreeComponent<IEspDeviceTreeElement>>();
        // alloc elements
        for (IEspDeviceTreeElement elementInList : elementList)
        {
            // display from level 1
            if (elementInList.getLevel() < 1)
            {
                continue;
            }
            IEspTreeComponent<IEspDeviceTreeElement> treeComponent = null;
            if (elementInList.isHasChild())
            {
                treeComponent = new EspTreeComposite<IEspDeviceTreeElement>(elementInList);
            }
            else
            {
                treeComponent = new EspTreeLeaf<IEspDeviceTreeElement>(elementInList);
            }
            treeComponentList.add(treeComponent);
        }
        // build tree
        for (IEspTreeComponent<IEspDeviceTreeElement> treeComponentInList : treeComponentList)
        {
            IEspDeviceTreeElement element = treeComponentInList.getEspElement();
            if (element.isHasParent() && element.getLevel() > 1)
            {
                String parentId = element.getParentId();
                IEspTreeComponent<IEspDeviceTreeElement> parent = __getTreeComponentById(treeComponentList, parentId);
                treeComponentInList.attachToParent(parent);
            }
        }
        // add the root tree(don't merge the for clause to build tree)
        for (IEspTreeComponent<IEspDeviceTreeElement> treeComponentInList : treeComponentList)
        {
            IEspDeviceTreeElement element = treeComponentInList.getEspElement();
            if (element.getLevel() == 1)
            {
                
                mTreeElements.add(treeComponentInList);
            }
        }
    }
    
    // add tree element recursively
    private void __addTreeElementRec(IEspTreeComponent<IEspDeviceTreeElement> treeElement)
    {
        IEspDeviceTreeElement element = treeElement.getEspElement();
        if (!mCurrentElements.contains(element))
        {
            mCurrentElements.add(element);
        }
        // only show the fold element
        if (treeElement.getEspElement().isFold())
        {
            for (IEspTreeComponent<IEspDeviceTreeElement> childTreeElement : treeElement.getChildrenList())
            {
                __addTreeElementRec(childTreeElement);
            }
        }
    }
    
    // update the current elements to be displayed
    private void __updateCurrentElements()
    {
        mCurrentElements.clear();
        for (IEspTreeComponent<IEspDeviceTreeElement> treeElementInList : mTreeElements)
        {
            __addTreeElementRec(treeElementInList);
        }
    }
    
    private TreeViewAdapter mAdapter = null;
    
    private LastLevelItemClickListener mItemClickCallBack;
    private LastLevelItemLongClickListener mItemLongClickCallBack;
    
    public TreeView(final Context context, AttributeSet attrs)
    {
        super(context, attrs);
        mAdapter = new TreeViewAdapter(context);
        this.setAdapter(mAdapter);
        this.setOnItemClickListener(this);
    }
    
    public void initData(Context context, List<IEspDeviceTreeElement> treeElements)
    {
        __buildTree(treeElements);
        __updateCurrentElements();
        mAdapter.notifyDataSetChanged();
    }
    
    public void notifyDataSetChanged(List<IEspDeviceTreeElement> newTreeElements)
    {
        mTreeElements.clear();
        __buildTree(newTreeElements);
        __updateCurrentElements();
        mAdapter.notifyDataSetChanged();
    }
    
    public void setExpandable(boolean expandable)
    {
        setAllElementFold(mTreeElements, expandable);
        __updateCurrentElements();
        mAdapter.notifyDataSetChanged();
    }
    
    private void setAllElementFold(List<IEspTreeComponent<IEspDeviceTreeElement>> list, boolean fold)
    {
        for (IEspTreeComponent<IEspDeviceTreeElement> component : list)
        {
            IEspDeviceTreeElement element = component.getEspElement();
            if (element.isHasChild())
            {
                element.setFold(fold);
                setAllElementFold(component.getChildrenList(), fold);
            }
        }
    }
    
    /**
     * 
     * Set item click listener callback
     * 
     * @param itemClickCallBack
     */
    public void setLastLevelItemClickCallBack(LastLevelItemClickListener itemClickCallBack)
    {
        this.mItemClickCallBack = itemClickCallBack;
    }
    
    public void setLastLevelItemLongClickCallBack(LastLevelItemLongClickListener itemLongClickCallBack)
    {
        if (itemLongClickCallBack == null)
        {
            setOnItemLongClickListener(null);
        }
        else
        {
            mItemLongClickCallBack = itemLongClickCallBack;
            setOnItemLongClickListener(this);
        }
    }
    
    /**
     * Init TreeView list data, set first level elements in currentElements
     */
    
    @Override
    public void onItemClick(AdapterView<?> arg0, View convertView, int position, long id)
    {
        IEspDeviceTreeElement element = mCurrentElements.get(position);
        IEspDeviceState deviceState = element.getCurrentDevice().getDeviceState();
        if (deviceState.isStateUpgradingInternet() || deviceState.isStateUpgradingLocal())
        {
            return;
        }
        
        if (mItemClickCallBack != null)
        {
            mItemClickCallBack.onLastLevelItemClick(element, position);
        }
    }
    
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
    {
        IEspDeviceTreeElement element = mCurrentElements.get(position);
        if (mItemLongClickCallBack != null)
        {
            return mItemLongClickCallBack.onLastLevelItemLongClick(element, position);
        }
        return false;
    }
    
    /**
     * interface for user click the node callback
     */
    public interface LastLevelItemClickListener
    {
        public void onLastLevelItemClick(IEspDeviceTreeElement element, int position);
    }
    
    public interface LastLevelItemLongClickListener
    {
        public boolean onLastLevelItemLongClick(IEspDeviceTreeElement element, int position);
    }
    
    public class TreeItemForlerListener implements View.OnClickListener
    {
        
        private int mPosition;
        
        public TreeItemForlerListener(int position)
        {
            mPosition = position;
        }
        
        @Override
        public void onClick(View v)
        {
            IEspDeviceTreeElement element = mCurrentElements.get(mPosition);
            element.setFold(!element.isFold());
            __updateCurrentElements();
            mAdapter.notifyDataSetChanged();
        }
        
    }
    
    public class TreeViewAdapter extends BaseAdapter
    {
        private class ViewHolder
        {
            ImageView icon;
            
            TextView title;
            
            ImageView status;
        }
        
        private Context context;
        
        private ViewHolder holder;
        
        private LayoutInflater inflater;
        
        // private List<IEspDeviceTreeElement> mElemetns;
        
        public TreeViewAdapter(Context context)
        {
            
            this.context = context;
        }
        
        @Override
        public int getCount()
        {
            return mCurrentElements.size();
        }
        
        @Override
        public Object getItem(int position)
        {
            return mCurrentElements.get(position);
        }
        
        @Override
        public long getItemId(int position)
        {
            return position;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            if (convertView == null)
            {
                if (inflater == null)
                {
                    inflater = LayoutInflater.from(context);
                }
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.tree_view_item_layout, parent, false);
                holder.icon = (ImageView)convertView.findViewById(R.id.tree_view_item_icon);
                holder.title = (TextView)convertView.findViewById(R.id.tree_view_item_title);
                holder.status = (ImageView)convertView.findViewById(R.id.tree_view_item_status);
                convertView.setTag(holder);
            }
            else
            {
                holder = (ViewHolder)convertView.getTag();
            }
            
            IEspDeviceTreeElement element = mCurrentElements.get(position);
            
            if (element.isHasChild())
            {
                if (element.isFold())
                {
                    holder.icon.setImageResource(R.drawable.esp_tree_icon_fold_on);
                }
                else if (!element.isFold())
                {
                    holder.icon.setImageResource(R.drawable.esp_tree_icon_fold_off);
                }
                holder.icon.setVisibility(View.VISIBLE);
            }
            else
            {
                holder.icon.setImageResource(R.drawable.esp_tree_icon_fold_off);
                holder.icon.setVisibility(View.INVISIBLE);
            }
            
            int basePadding = context.getResources().getDimensionPixelSize(R.dimen.esp_treeview_icon_base_padding);
            int baseTextSize = context.getResources().getDimensionPixelSize(R.dimen.esp_treeview_text_size);
            int iconVerticalPadding =
                context.getResources().getDimensionPixelSize(R.dimen.esp_treeview_icon_vertical_padding);
            holder.icon.setPadding(basePadding * (element.getLevel()), iconVerticalPadding, 0, iconVerticalPadding);
            holder.icon.setOnClickListener(new TreeView.TreeItemForlerListener(position));
            holder.title.setText(element.getTitle());
            holder.title.setTextSize(TypedValue.COMPLEX_UNIT_PX, baseTextSize - element.getLevel() * 3);
            
            IEspDevice device = element.getCurrentDevice();
            if (device.getDeviceState().isStateUpgradingInternet() || device.getDeviceState().isStateUpgradingLocal())
            {
                holder.title.append(" (upgrading...) ");
            }
            if (device.isActivated())
            {
                holder.status.setBackgroundResource(R.drawable.esp_icon_cloud);
            }
            else
            {
                holder.status.setBackgroundResource(R.drawable.esp_icon_local);
            }
            
            return convertView;
        }
        
    }
    
}