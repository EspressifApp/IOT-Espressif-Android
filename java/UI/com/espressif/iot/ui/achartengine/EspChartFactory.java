package com.espressif.iot.ui.achartengine;

import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.achartengine.util.MathHelper;

import com.espressif.iot.R;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.view.View;

public class EspChartFactory
{
    private static final int X_AXIS_LENGTH = 10;
    
    private Context mContext;
    
    private XYMultipleSeriesRenderer mSingleLineCacheRenderer;
    
    private XYMultipleSeriesRenderer mMultipleLineCacheRenderer;
    
    public EspChartFactory(Context context)
    {
        mContext = context;
    }
    
    /**
     * Get single Line ChartView
     * 
     * @param data
     * @return
     */
    public View getSingleLineChartView(ChartData data)
    {
        XYMultipleSeriesRenderer XYRenderer = new XYMultipleSeriesRenderer();
        
        double xMin;
        double xMax = 0;
        double yMin = Double.MAX_VALUE;
        double yMax = Double.MIN_VALUE;
        
        String title = data.getTitle();
        XYSeries series = new XYSeries(title);
        String YTitle = data.getYTitle();
        XYRenderer.setYTitle(YTitle);
        
        List<ChartPoint> pointList = data.getPointList();
        XYRenderer.setXLabels(0);
        for (ChartPoint point : pointList)
        {
            // Set X Point Label
            XYRenderer.addXTextLabel(point.getX(), point.getXLabel());
            
            series.add(point.getX(), point.getY());
            
            if (point.getY() != MathHelper.NULL_VALUE)
            {
                xMax = point.getX();
                
                yMin = Math.min(yMin, point.getY());
                yMax = Math.max(yMax, point.getY());
            }
        }
        
        // Set X and Y Axis length and position
        if (xMax < X_AXIS_LENGTH)
        {
            xMax = X_AXIS_LENGTH;
        }
        xMin = xMax - X_AXIS_LENGTH;
        if (yMin > yMax)
        {
            yMin = 0;
            yMax = 1;
        }
        XYRenderer.setXAxisMax(xMax);
        XYRenderer.setXAxisMin(xMin);
        XYRenderer.setYAxisMax(yMax + 1);
        XYRenderer.setYAxisMin(yMin - 1);
        
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        dataset.addSeries(series);
        
        XYSeriesRenderer renderer = new XYSeriesRenderer();
        renderer.setPointStyle(PointStyle.POINT);
        renderer.setFillPoints(true);
        // Set line width
        renderer.setLineWidth(2.5f);
        // Show data on point
        // renderer.setDisplayChartValues(true);
        // Set line color
        renderer.setColor(data.getColor());
        
        XYRenderer.addSeriesRenderer(renderer);
        double[] panLimits = new double[] {0, pointList.size(), yMin, yMax};
        XYRenderer.setPanLimits(panLimits);
        configureRenderer(XYRenderer);
        
        XYRenderer.setYLabelsAlign(Align.RIGHT);
        
        // Reset cache data
        if (mSingleLineCacheRenderer != null)
        {
            double cacheXmin = mSingleLineCacheRenderer.getXAxisMin();
            double cacheXmax = mSingleLineCacheRenderer.getXAxisMax();
            if (cacheXmax > xMax)
            {
                cacheXmax = xMax;
                cacheXmin = xMin;
            }
            XYRenderer.setXAxisMin(cacheXmin);
            XYRenderer.setXAxisMax(cacheXmax);
        }
        mSingleLineCacheRenderer = XYRenderer;
        
        return ChartFactory.getCubeLineChartView(mContext, dataset, XYRenderer, 0.5f);
    }
    
    /**
     * Get double lines of two sides Y axis ChartView
     * 
     * @param datas : must contain two elements
     * @return
     */
    public View getMultipleLinesChartView(ChartData[] datas)
    {
        int length = datas.length;
        
        if (length < 2)
        {
            throw new NullPointerException("Must need two ChartData");
        }
        else if (length > 2)
        {
            throw new ArrayIndexOutOfBoundsException("Only need two ChartData");
        }
        
        double xMin;
        double xMax = 0;
        double yMin = Double.MAX_VALUE;
        double yMax = Double.MIN_VALUE;
        
        XYMultipleSeriesRenderer XYRenderer = new XYMultipleSeriesRenderer(length);
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        
        // Set X Axis label
        List<ChartPoint> pointList = datas[0].getPointList();
        XYRenderer.setXLabels(0);
        for (int i = 0; i < pointList.size(); i++)
        {
            ChartPoint point = pointList.get(i);
            XYRenderer.addXTextLabel(point.getX(), point.getXLabel());
        }
        
        // Add Two Line
        for (int i = 0; i < length; i++)
        {
            ChartData data = datas[i];
            
            XYSeriesRenderer r = new XYSeriesRenderer();
            r.setColor(data.getColor());
            r.setPointStyle(PointStyle.POINT);
            r.setLineWidth(2.5f);
            XYRenderer.addSeriesRenderer(r);
            
            XYRenderer.setYTitle(data.getTitle(), i);
            XYRenderer.setYLabelsColor(i, data.getColor());
            
            XYSeries series = new XYSeries(data.getTitle(), i);
            for (ChartPoint cp : data.getPointList())
            {
                series.add(cp.getX(), cp.getY());
                
                if (cp.getY() != MathHelper.NULL_VALUE)
                {
                    xMax = cp.getX();
                    
                    yMin = Math.min(yMin, cp.getY());
                    yMax = Math.max(yMax, cp.getY());
                }
            }
            dataset.addSeries(series);
        }
        
        // Set X and Y Axis length and position
        if (xMax < X_AXIS_LENGTH)
        {
            xMax = X_AXIS_LENGTH;
        }
        xMin = xMax - X_AXIS_LENGTH;
        if (yMin > yMax)
        {
            yMin = 0;
            yMax = 1;
        }
        for (int i = 0; i < length; i++)
        {
            XYRenderer.setXAxisMax(xMax, i);
            XYRenderer.setXAxisMin(xMin, i);
            XYRenderer.setYAxisMax(yMax + 1, i);
            XYRenderer.setYAxisMin(yMin - 1, i);
        }
        
        XYRenderer.setYAxisAlign(Align.LEFT, 0);
        XYRenderer.setYLabelsAlign(Align.RIGHT, 0);
        // Set a Line to Right Y Axis
        XYRenderer.setYAxisAlign(Align.RIGHT, 1);
        XYRenderer.setYLabelsAlign(Align.LEFT, 1);
        
        double[] panLimits = new double[] {0, pointList.size(), yMin, yMax};
        XYRenderer.setPanLimits(panLimits);
        configureRenderer(XYRenderer);
        
        // Reset cache data
        if (mMultipleLineCacheRenderer != null)
        {
            double cacheXmin = mMultipleLineCacheRenderer.getXAxisMin();
            double cacheXmax = mMultipleLineCacheRenderer.getXAxisMax();
            if (cacheXmax > xMax)
            {
                cacheXmax = xMax;
                cacheXmin = xMin;
            }
            XYRenderer.setXAxisMin(cacheXmin, 0);
            XYRenderer.setXAxisMax(cacheXmax, 0);
            XYRenderer.setXAxisMin(cacheXmin, 1);
            XYRenderer.setXAxisMax(cacheXmax, 1);
        }
        mMultipleLineCacheRenderer = XYRenderer;
        
        return ChartFactory.getCubeLineChartView(mContext, dataset, XYRenderer, 0.5f);
    }
    
    /**
     * Set some ChartView settings
     * 
     * @param XYRenderer
     */
    private void configureRenderer(XYMultipleSeriesRenderer XYRenderer)
    {
        XYRenderer.setAxesColor(Color.LTGRAY);
        XYRenderer.setLabelsColor(Color.LTGRAY);
        XYRenderer.setZoomEnabled(false, false);
        XYRenderer.setAxisTitleTextSize(16);
        XYRenderer.setChartTitleTextSize(20);
        XYRenderer.setLabelsTextSize(15);
        XYRenderer.setLegendTextSize(15);
        XYRenderer.setPointSize(5f);
        int margin = mContext.getResources().getDimensionPixelSize(R.dimen.chart_title_margin);
        XYRenderer.setMargins(new int[] {margin, margin, margin, margin});
        XYRenderer.setShowGrid(true);
        XYRenderer.setXLabelsAlign(Align.RIGHT);
        XYRenderer.setYLabels(20);
        // if in ScrollView, need set
        XYRenderer.setInScroll(true);
    }
    
}
