/*
 * Copyright (c) 2003-2012 Fred Hutchinson Cancer Research Center
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fhcrc.cpl.viewer.gui;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.util.Iterator;

/**
 * User: mbellew
 * Date: Jun 29, 2004
 * Time: 12:02:33 PM
 */
public class SpectrumChartFactory
{
    static Logger _log = Logger.getLogger(SpectrumChartFactory.class);
    static Color[] _colors = new Color[]{Color.BLUE, Color.RED, Color.GREEN};


    public static ChartPanel CreateChartPanel(XYDataset dataset)
    {
        return CreateChartPanel(dataset, _colors);
    }


    public static ChartPanel CreateChartPanel(XYDataset dataset, Color[] colors)
    {
        XYPlot xy = createXYPlot(dataset, colors);
        JFreeChart chart = new JFreeChart(xy);
        ChartPanel chartPanel = new SpectrumChartPanel(chart);
        chartPanel.setDisplayToolTips(true);
        chartPanel.setMouseZoomable(true);
        // Remove the autogenerated subtitle
        if (chart.getSubtitleCount() == 1)
            chart.removeSubtitle(chart.getSubtitle(chart.getSubtitleCount()-1));
        return chartPanel;
    }


    public static ChartPanel CreateChartPanel(java.util.List datasets, Color[] colors)
    {
        if (datasets.size() == 1)
            return CreateChartPanel((XYSeriesCollection)datasets.get(0), colors);

        CombinedDomainXYPlot combined = new CombinedDomainXYPlot();
        for (Iterator it = datasets.iterator(); it.hasNext();)
        {
            XYSeriesCollection series = (XYSeriesCollection)it.next();
            XYPlot xy = createXYPlot(series, colors);
            combined.add(xy);
        }
        NumberAxis axisDomain = new NumberAxis();
        axisDomain.setAutoRangeIncludesZero(false);
//		axisDomain.setRange(400.0, 1600.0);
        combined.setDomainAxis(axisDomain);

        JFreeChart chart = new JFreeChart(combined);
        ChartPanel chartPanel = new SpectrumChartPanel(chart);
        chartPanel.setDisplayToolTips(true);
        chartPanel.setMouseZoomable(true);
        // Remove the autogenerated subtitle
        if (chart.getSubtitleCount() == 1)
            chart.removeSubtitle(chart.getSubtitle(chart.getSubtitleCount()-1));
        return chartPanel;
    }


    static class SpectrumChartPanel extends ChartPanel
    {
        SpectrumChartPanel(JFreeChart chart)
        {
            super(chart);
        }
    }


    ///UNDONE: pass in two XYDatasets in the first place
    protected static XYPlot createXYPlot(XYDataset dataset, Color[] colors)
    {
        // break up into two datasets, one for bars one for lines
        //LinkedList lines =new LinkedList();
        //LinkedList bars = new LinkedList();
        XYDataset seriesLines = new XYSeriesCollection();
        XYDataset seriesBars = new XYSeriesCollection();
        ((XYSeriesCollection)seriesBars).setIntervalWidth(0.0);

        if (dataset instanceof XYSeriesCollection)
        {
            while (0 < dataset.getSeriesCount())
            {
                XYSeries s = ((XYSeriesCollection)dataset).getSeries(0);
                ((XYSeriesCollection)dataset).removeSeries(0);
                Comparable key = s.getKey();
                boolean lines = false;
                if (key instanceof String)
                    lines = ((String)key).startsWith("-");
                if (lines)
                    ((XYSeriesCollection)seriesLines).addSeries(s);
                else
                    ((XYSeriesCollection)seriesBars).addSeries(s);
            }
        }
        else
        {
            seriesBars = dataset;
        }

        NumberAxis axisDomain = new NumberAxis();
        axisDomain.setAutoRange(true);
        axisDomain.setAutoRangeIncludesZero(false);
//		axisDomain.setRange(400.0, 1600.0);
        // NOTE: zooming in too far kills the chart, prevent this
        axisDomain.addChangeListener(new AxisChangeListener()
        {
            public void axisChanged(AxisChangeEvent event)
            {
                NumberAxis axis = (NumberAxis)event.getSource();
                Range range = axis.getRange();
                if (range.getLength() < 2.0)
                {
                    //_log.info("AxisChangeListener " + range.getLength() + " " + range.toString());
                    double middle = range.getLowerBound() + range.getLength() / 2.0;
                    axis.setRange(new Range(middle - 1.1, middle + 1.1));
                }
            }
        });

        NumberAxis axisRange = new NumberAxis();
        axisRange.setAutoRange(true);
        axisRange.setAutoRangeIncludesZero(true);

        XYToolTipGenerator toolTipGenerator = new XYToolTipGenerator()
        {
            public String generateToolTip(XYDataset xyDataset, int s, int i)
            {
                double X = Math.round(xyDataset.getXValue(s, i) * 1000.0) / 1000.0;
                double Y = Math.round(xyDataset.getYValue(s, i) * 1000.0) / 1000.0;
                return "(" + X + ", " + Y + ")";
            }
        };



        XYBarRenderer barRenderer = new XYBarRenderer();
        //dhmay adding 2009/09/14.  As of jfree 1.0.13, shadows on by default        
        barRenderer.setShadowVisible(false);


        //dhmay adding for jfreechart 1.0.6 upgrade.  If this isn't here, we get a
        //nullPointerException in XYBarRenderer.drawItemLabel
        barRenderer.setBaseItemLabelGenerator(new NullLabelGenerator());

        barRenderer.setSeriesItemLabelsVisible(0, true);
        barRenderer.setBaseToolTipGenerator(toolTipGenerator);

        XYLineAndShapeRenderer lineRenderer = new XYLineAndShapeRenderer();
        lineRenderer.setBaseToolTipGenerator(toolTipGenerator);

        XYPlot xy = new XYPlot(null, axisDomain, axisRange, null);

        int ds = 0;
        if (seriesLines.getSeriesCount() > 0)
        {
            xy.setDataset(ds, seriesLines);
            xy.setRenderer(ds, lineRenderer);
            xy.mapDatasetToRangeAxis(ds, 0);
            ds++;
            for (int i = 0; i < seriesLines.getSeriesCount(); i++)
            {
                Comparable key =  ((XYSeriesCollection)seriesLines).getSeriesKey(i);
                boolean lines = false;
                if (key instanceof String)
                    lines = ((String)key).startsWith("-");
                lineRenderer.setSeriesLinesVisible(i, lines);
                lineRenderer.setSeriesShapesVisible(i, !lines);
            }
        }
        if (seriesBars.getSeriesCount() > 0)
        {
            xy.setDataset(ds, seriesBars);
            xy.setRenderer(ds, barRenderer);
            xy.mapDatasetToRangeAxis(ds, 0);
            ds++;
        }

        setColors(xy, colors);

        return xy;
    }

    /**
     * This should not be necessary.  dhmay creating this class for the jfreechart 1.0.6 upgrade.
     * We shouldn't have to specify a labelgenerator at all for the barchart
     *
     */
    protected static class NullLabelGenerator implements XYItemLabelGenerator
    {
        public String generateLabel(XYDataset dataset,
                                    int series,
                                    int item)
        {
            return null;
        }
    }


    public static void setColors(ChartPanel panel, Color[] colors)
    {
        Plot plot = panel.getChart().getPlot();
        if (plot instanceof XYPlot)
        {
            setColors((XYPlot)plot, colors);
            return;
        }

        CombinedDomainXYPlot plotCombined = (CombinedDomainXYPlot)plot;
        java.util.List list = (plotCombined).getSubplots();
        for (int i = 0; i < list.size(); i++)
            setColors((XYPlot)list.get(i), colors);
    }


    public static void setColors(XYPlot xy, Color[] colors)
    {
        for (int c = 0; c < colors.length; c++)
        {
            XYItemRenderer r = xy.getRenderer(c);
            if (null == r) continue;
            r.setSeriesPaint(c, colors[c]);
        }
    }
}
