/**
 * Created on: Nov 05, 2015
 * Author: Antony Mykolaj
 */
package de.codecrafters.tableview;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.codecrafters.tableview.colorizers.TableDataRowColorizer;

/**
 *
 * @param <K> Type of an object representing a single group. Must implement equals() and hashCode() methods
 * @param <T>
 */
public abstract class GroupTableDataAdapter<K, T> extends BaseExpandableListAdapter {

    private static final String LOG_TAG = GroupTableDataAdapter.class.getSimpleName();
    private final Context mContext;
    private final Map<K, List<T>> mData;
    private TableColumnModel mColumnModel;
    private TableDataRowColorizer rowColoriser;

    protected GroupTableDataAdapter(final Context context, TableColumnModel columnModel, Map<K, List<T>> data) {
        mContext = context;
        mColumnModel = columnModel;
        if (data != null) {
            mData = new LinkedHashMap<K, List<T>>(data);
        } else {
            mData = new LinkedHashMap<K, List<T>>();
        }
    }

    @Override
    public int getGroupCount() {
        return mData.keySet().size();
    }

    @Override
    public int getChildrenCount(final int groupPosition) {
        return getGroup(groupPosition).getValue().size();
    }

    @Override
    public Map.Entry<K, List<T>> getGroup(final int groupPosition) {
        // TODO Rework this and optimize. Maybe use two arrays or something like that.
        Set<Map.Entry<K, List<T>>> entries = mData.entrySet();
        Iterator<Map.Entry<K, List<T>>> iterator = entries.iterator();
        int counter = 0;

        while (iterator.hasNext()) {
            Map.Entry<K, List<T>> kvPair = iterator.next();
            if (counter == groupPosition) {
                return kvPair;
            }
            counter++;
        }

        return null;
    }

    @Override
    public T getChild(final int groupPosition, final int childPosition) {
        return getGroup(groupPosition).getValue().get(childPosition);
    }

    @Override
    public long getGroupId(final int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(final int groupPosition, final int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition,
            final boolean isLastChild, final View convertView, final ViewGroup parent) {

        final LinearLayout rowView = new LinearLayout(getContext());
        final AbsListView.LayoutParams rowLayoutParams = new AbsListView.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rowView.setLayoutParams(rowLayoutParams);
        rowView.setGravity(Gravity.CENTER_VERTICAL);

        T rowData = null;
        try {
            rowData = getChild(groupPosition, childPosition);
        } catch (final IndexOutOfBoundsException e) {
            Log.w(LOG_TAG, "No data available for group " + groupPosition +  ", row " + childPosition
                    + ". Caught Exception: " + e.getMessage());
        }
        rowView.setBackgroundColor(rowColoriser.getRowColor(childPosition, rowData));
        final int widthUnit = (parent.getWidth() / mColumnModel.getColumnWeightSum());

        for (int columnIndex = 0; columnIndex < getColumnCount(); columnIndex++) {
            View cellView = getCellView(groupPosition, childPosition, columnIndex, rowView);
            if (cellView == null) {
                cellView = new View(getContext());
            }

            final int width = widthUnit * mColumnModel.getColumnWeight(columnIndex);
            final LinearLayout.LayoutParams cellLayoutParams = new LinearLayout.LayoutParams(width,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            cellLayoutParams.weight = mColumnModel.getColumnWeight(columnIndex);
            cellView.setLayoutParams(cellLayoutParams);
            rowView.addView(cellView);
        }

        return rowView;
    }

    @Override
    public boolean isChildSelectable(final int groupPosition, final int childPosition) {
        return true;
    }

    public abstract View getCellView(int groupPosition, int rowIndexInGroup, int columnIndex,
            ViewGroup parentView);

    public LayoutInflater getLayoutInflater() {
        return (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    protected int getColumnCount() {
        return mColumnModel.getColumnCount();
    }

    /**
     * Sets the column count which is used to render the table headers.
     *
     * @param columnCount The column count that should be set.
     */
    protected void setColumnCount(final int columnCount) {
        mColumnModel.setColumnCount(columnCount);
    }

    public final Context getContext() {
        return mContext;
    }

    /**
     * Sets the {@link TableDataRowColorizer} that will be used to colorise the table data rows.
     *
     * @param rowColorizer
     *         The {@link TableDataRowColorizer} that shall be used.
     */
    protected void setRowColoriser(final TableDataRowColorizer rowColorizer) {
        this.rowColoriser = rowColorizer;
    }

    /**
     * Gives the {@link TableColumnModel} that is currently used to render the table headers.
     */
    protected TableColumnModel getColumnModel() {
        return mColumnModel;
    }

    /**
     * Sets the {@link TableColumnModel} that will be used to render the table cells.
     *
     * @param columnModel The {@link TableColumnModel} that should be set.
     */
    protected void setColumnModel(final TableColumnModel columnModel) {
        this.mColumnModel = columnModel;
    }

    /**
     * Gives the data that is set to this adapter.
     *
     * @return The data this adapter is currently working with.
     */
    public Map<K, List<T>> getData() {
        return mData;
    }

    public void addGroup(K group, List<T> groupData) {
        mData.put(group, groupData);
        notifyDataSetChanged();
    }

    public void removeGroup(K group) {
        mData.remove(group);
        notifyDataSetChanged();
    }

    public void clear() {
        mData.clear();
        notifyDataSetChanged();
    }

}
