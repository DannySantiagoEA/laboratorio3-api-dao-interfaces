package co.edu.univalle.ir.gui.view;

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
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Componente Swing basado en JFreeChart con ventana deslizante optimizada
 * (Tareas #37 y #39 - Entregable 3.d).
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.1 $ 2026/11/24
 */
public class GraficaTiempo extends JPanel {

    public static final double VENTANA_SEGUNDOS = 10.0;
    public static final int MAX_PUNTOS = 1000;

    private final XYSeries serie;
    private final JFreeChart grafica;
    private final boolean esDigital;
    private String tituloVisible;

    public GraficaTiempo(String etiquetaEjeY, double minY, double maxY, boolean esDigital, Color colorTrazo) {
        this.esDigital = esDigital;
        this.tituloVisible = etiquetaEjeY;

        this.serie = new XYSeries("Muestras DAO", false, true);
        this.serie.setMaximumItemCount(MAX_PUNTOS);
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

        // Eje X en modo osciloscopio: ventana deslizante fija de los ultimos 10 s (Tarea #39)
        NumberAxis ejeX = (NumberAxis) plot.getDomainAxis();
        ejeX.setAutoRange(true);
        ejeX.setFixedAutoRange(VENTANA_SEGUNDOS);
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
     * Tarea #39: agrega un lote de puntos {tiempo, valor} y ejecuta un solo
     * redibujo al final del lote para mantener fluido el EDT de Swing.
     *
     * @param puntos Lista de pares {tiempo (s), valor}.
     */
    public void agregarPuntos(List<double[]> puntos) {
        if (puntos == null || puntos.isEmpty()) {
            return;
        }
        for (double[] punto : puntos) {
            // Si una nueva corrida de simulacion reinicia el reloj en t=0, limpiar antes de trazar
            if (serie.getItemCount() > 0) {
                double ultimoT = serie.getX(serie.getItemCount() - 1).doubleValue();
                if (punto[0] < ultimoT) {
                    serie.clear();
                }
            }
            serie.add(punto[0], punto[1], false);
        }
        serie.fireSeriesChanged();
    }

    public void cambiarTitulo(String nuevoTitulo) {
        this.tituloVisible = nuevoTitulo;
        this.grafica.setTitle(nuevoTitulo);
    }

    public void limpiar() {
        serie.clear();
    }

    public int getCantidadPuntos() {
        return serie.getItemCount();
    }

    public List<double[]> getPuntosVisibles() {
        Range visible = grafica.getXYPlot().getDomainAxis().getRange();
        List<double[]> puntos = new ArrayList<>();
        for (int i = 0; i < serie.getItemCount(); i++) {
            double t = serie.getX(i).doubleValue();
            if (visible.contains(t)) {
                puntos.add(new double[]{t, serie.getY(i).doubleValue()});
            }
        }
        return puntos;
    }

    public String getDescripcionVisible() {
        return tituloVisible;
    }
}
