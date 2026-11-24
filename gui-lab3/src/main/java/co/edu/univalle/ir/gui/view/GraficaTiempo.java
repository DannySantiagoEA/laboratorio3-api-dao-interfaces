package co.edu.univalle.ir.gui.view;

import co.edu.univalle.ir.entity.int_proceso_vars_data;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYStepRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Componente Swing basado en JFreeChart para graficar series temporales (Valor
 * vs. Tiempo) leidas desde la tabla <code>int_proceso_vars_data</code> (HU-08 -
 * Tarea #37).
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.0 $ 2026/11/23
 */
public class GraficaTiempo extends JPanel {

    private final XYSeries serie;
    private final JFreeChart grafica;
    private final boolean esDigital;
    private String tituloVisible;

    /**
     * Crea un lienzo de graficacion JFreeChart configurado para senal analogica
     * o digital.
     *
     * @param etiquetaEjeY Etiqueta del eje vertical (ej. "Voltaje (V)" o "Nivel
     * Logico (0/1)").
     * @param minY Limite inferior fijo del eje Y.
     * @param maxY Limite superior fijo del eje Y.
     * @param esDigital true para renderizado escalonado discreto (0/1), false
     * para linea continua.
     * @param colorTrazo Color principal de la curva.
     */
    public GraficaTiempo(String etiquetaEjeY, double minY, double maxY, boolean esDigital, Color colorTrazo) {
        this.esDigital = esDigital;
        this.tituloVisible = etiquetaEjeY;

        // autoSort=false y allowDuplicateXValues=true evitan excepciones si dos muestras comparten tiempo
        this.serie = new XYSeries("Muestras DAO", false, true);
        XYSeriesCollection dataset = new XYSeriesCollection(serie);

        this.grafica = ChartFactory.createXYLineChart(
                etiquetaEjeY,
                "Tiempo (s)",
                etiquetaEjeY,
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );

        configurarEstilo(minY, maxY, colorTrazo);

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        ChartPanel chartPanel = new ChartPanel(grafica);
        chartPanel.setPreferredSize(new Dimension(480, 260));
        chartPanel.setMouseWheelEnabled(true);
        add(chartPanel, BorderLayout.CENTER);
    }

    private void configurarEstilo(double minY, double maxY, Color colorTrazo) {
        grafica.setBackgroundPaint(Color.WHITE);
        grafica.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 14));
        grafica.getTitle().setPaint(new Color(30, 41, 59));

        XYPlot plot = grafica.getXYPlot();
        plot.setBackgroundPaint(new Color(249, 250, 252));
        plot.setDomainGridlinePaint(new Color(215, 222, 232));
        plot.setRangeGridlinePaint(new Color(215, 222, 232));
        plot.setOutlinePaint(new Color(180, 190, 204));

        NumberAxis ejeX = (NumberAxis) plot.getDomainAxis();
        ejeX.setAutoRange(true);
        ejeX.setAutoRangeIncludesZero(false);
        ejeX.setLabelFont(new Font("Segoe UI", Font.BOLD, 12));
        ejeX.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));

        NumberAxis ejeY = (NumberAxis) plot.getRangeAxis();
        ejeY.setRange(minY, maxY);
        ejeY.setLabelFont(new Font("Segoe UI", Font.BOLD, 12));
        ejeY.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));

        if (esDigital) {
            XYStepRenderer stepRenderer = new XYStepRenderer();
            stepRenderer.setSeriesPaint(0, colorTrazo);
            stepRenderer.setSeriesStroke(0, new BasicStroke(2.2f));
            stepRenderer.setSeriesShapesVisible(0, true);
            stepRenderer.setSeriesShape(0, new Ellipse2D.Double(-2.5, -2.5, 5.0, 5.0));
            plot.setRenderer(stepRenderer);
        } else {
            XYLineAndShapeRenderer lineRenderer = new XYLineAndShapeRenderer(true, false);
            lineRenderer.setSeriesPaint(0, colorTrazo);
            lineRenderer.setSeriesStroke(0, new BasicStroke(2.1f));
            plot.setRenderer(lineRenderer);
        }
    }

    /**
     * Reemplaza limpiamente los puntos visibles con las muestras recuperadas
     * desde MySQL (int_proceso_vars_data), evitando parpadeos visuales.
     *
     * @param muestras Lista ordenada cronologicamente devuelta por
     * seleccionarUltimosPorVariable().
     */
    public void actualizarDesdeMuestras(List<int_proceso_vars_data> muestras) {
        serie.setNotify(false);
        try {
            serie.clear();
            if (muestras != null && !muestras.isEmpty()) {
                // Si hubo un reinicio del puente RS232 (t vuelve a 0), mostrar solo la corrida mas reciente
                int indiceInicio = 0;
                for (int i = muestras.size() - 1; i > 0; i--) {
                    if (muestras.get(i).getTiempo() < muestras.get(i - 1).getTiempo()) {
                        indiceInicio = i;
                        break;
                    }
                }
                for (int i = indiceInicio; i < muestras.size(); i++) {
                    int_proceso_vars_data m = muestras.get(i);
                    serie.add(m.getTiempo(), m.getValor(), false);
                }
            }
        } finally {
            serie.setNotify(true);
        }
    }

    /**
     * Cambia el titulo visible de la grafica al conmutar de canal.
     *
     * @param nuevoTitulo Nombre del canal seleccionado.
     */
    public void cambiarTitulo(String nuevoTitulo) {
        this.tituloVisible = nuevoTitulo;
        this.grafica.setTitle(nuevoTitulo);
    }

    /**
     * Limpia todos los puntos de la serie actual.
     */
    public void limpiar() {
        serie.clear();
    }

    /**
     * Retorna una copia de los pares (tiempo, valor) actualmente visibles en la
     * grafica.
     *
     * @return Lista de arreglos double[]{tiempo, valor}.
     */
    public List<double[]> getPuntosVisibles() {
        List<double[]> puntos = new ArrayList<>(serie.getItemCount());
        for (int i = 0; i < serie.getItemCount(); i++) {
            puntos.add(new double[]{serie.getX(i).doubleValue(), serie.getY(i).doubleValue()});
        }
        return puntos;
    }

    public String getDescripcionVisible() {
        return tituloVisible;
    }
}
