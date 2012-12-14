package hymas.m1.collecter;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Chirila Alexandru
 */
public class Collecter {

    private File file;
    private Label tagName;
    private Document doc;
    private Element root;

    /**
     * Creaza un colector ce va aduna datele si le va salva in formatul xml
     * impus in fisierul fileName, considerand situatia de miscare data de
     * actionName.
     *
     * @param fileName numele fisierului
     * @param actionName situatia de miscare corespunzatoare
     */
    public Collecter(String fileName, Label actionName) {
        this(new File(fileName), actionName);
    }

    /**
     * Creaza un colector ce va aduna datele si le va salva in formatul xml
     * impus in fisierul fileName, considerand situatia de miscare data de
     * actionName.
     *
     * @param file fisierul in care se vor salva datele
     * @param actionName situatia de miscare corespunzatoare
     */
    public Collecter(File file, Label actionName) {
        this.file = file;
        this.tagName = actionName;
    }

    /**
     * Pregateste documentul pentru colectarea de date.
     */
    public void startCollecting() {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            //create root element
            doc = docBuilder.newDocument();
            root = doc.createElement(tagName.name());
            doc.appendChild(root);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Collecter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Opreste colectarea si salveaza toate datele in fisier.
     */
    public void stopCollecting() {
        try {
            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(file);
            transformer.transform(source, result);            
        } catch (TransformerException ex) {
            Logger.getLogger(Collecter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Adauga un element de tip gps la documentul current
     *
     * @param latitude [-90.0,90.0] in grade
     * @param longitude [-180.0,180.0] in grade
     * @param altitude altitudinea in metri (calculata deaspra nivelului marii),
     * 0.0 daca nu este disponibil
     * @param bearing directia orizontala de deplasare in grade (0.0,360.0], 0.0
     * daca nu este disponibil
     * @param accuracy raza in metri a unui cerc cu centrul in pozitia data, in
     * care putem spune cu o probabilitate de 68% ca este pozitia corecta
     * @param timestamp timpul in milisecunde
     */
    public void addGpsData(double latitude, double longitude, double altitude,
            double bearing, double accuracy, long timestamp) {
        try {
            Element e = doc.createElement("gps");
            e.setAttribute("lat", Double.toString(latitude));
            e.setAttribute("long", Double.toString(longitude));
            if (altitude != 0.0) {
                e.setAttribute("alt", Double.toString(altitude));
            }
            if (bearing != 0.0) {
                e.setAttribute("bea", Double.toString(bearing));
            }
            e.setAttribute("acu", Double.toString(accuracy));
            e.setAttribute("time", Long.toString(timestamp));
            addElement(e);
        } catch (NullPointerException ex) {
            throw new NullPointerException("Metoda startCollecting nu a fost apelata");
        }
    }

    /**
     * Masoara acceleratia relativa a device-ului pe axele x,y,z in m/s^2
     * <br/><br/> Se ia in calcul si atractia gravitationala, deci pentru un
     * device care sta pe o suprafata plata, vor fi urmatoarele valori: <br/> x
     * ~= 0.0 , y ~= 0.0 , z ~= 9.8 <br/><br/> Explicatie pentru cum sunt
     * reprezentate axele gasiti <a
     * href="http://developer.android.com/reference/android/hardware/SensorEvent.html">
     * aici </a>.
     *     
* @param x
     * @param y
     * @param z
     * @param timestamp
     */
    public void addAccelerometerData(double x, double y, double z, long timestamp) {
        addCoordElement("acc", x, y, z, timestamp);
    }

    /**
     * Masoara campul magnetic ambiental pe cele 3 axe, valorile sunt in
     * micro-Tesla (μT)
     *     
* @param x
     * @param y
     * @param z
     * @param timestamp
     */
    public void addMagneticFieldData(double x, double y, double z, long timestamp) {
        addCoordElement("mag", x, y, z, timestamp);
    }

    /**
     * Masoara rata de rotatie a device-ului pe cele trei axe <br/><br/> Rotatie
     * este pozitiva in sensul invers acelor de ceasornic. <br/><br/> Fiecare
     * parametru reprezinta valoarea angulara in radiani pe secunda
     *
     * @param x
     * @param y
     * @param z
     * @param timestamp
     */
    public void addGyroscopeData(double x, double y, double z, long timestamp) {
        addCoordElement("gyro", x, y, z, timestamp);
    }

    /**
     * Masoara intesitatea ambientala a luminii in unitati lux
     *
     * @param value
     * @param timestamp
     */
    public void addLightData(double value, long timestamp) {
        addValueElement("light", value, timestamp);
    }

    /**
     * Masoara presiunea atmosferica in milibari (hPa)
     *
     * @param value
     * @param timestamp
     */
    public void addPressureData(double value, long timestamp) {
        addValueElement("pres", value, timestamp);
    }

    /**
     * Deobicei este <ul><li> 0.0 cand device-ul este acoperit, in buzunar, la
     * ureche etc..</li><li>1.0 cand device-ul este neacoperit.</li></ul>
     *
     * @param value
     * @param timestamp
     */
    public void addProximityData(double value, long timestamp) {
        addValueElement("prox", value, timestamp);
    }

    /**
     * Masoara atractie gravitationala pe cele 3 axe in m/s^2
     *
     * @param x
     * @param y
     * @param z
     * @param timestamp
     */
    public void addGravityData(double x, double y, double z, long timestamp) {
        addCoordElement("grav", x, y, z, timestamp);
    }

    /**
     * Masoara acceleratia liniara a device-ului fara sa ia in considerare si
     * atractie gravitationala. Se masoara in m/s^2. <br/><br/> Reprezinta
     * viteza cu care se deplaseaza intr-o anumita directie.
     *
     * @param x
     * @param y
     * @param z
     * @param timestamp
     */
    public void addLinearAccelerationData(double x, double y, double z, long timestamp) {
        addCoordElement("linear", x, y, z, timestamp);
    }

    /**
     * Un vector ce reprezinta orientarea device-ului ca o combinatie intre
     * unghi si axa. <br/><br/> x*sin(θ/2), y*sin(θ/2), z*sin(θ/2)<br/> unde θ -
     * reprezinta unghiul
     *
     * @param x
     * @param y
     * @param z
     * @param timestamp
     */
    public void addVectorRotationData(double x, double y, double z, long timestamp) {
        addCoordElement("vect", x, y, z, timestamp);
    }

    /**
     * Masoara umiditatea relativa a aerului in procente.
     *
     * @param value [0.0%,100.0%] procentul de umiditate din aer
     * @param timestamp
     */
    public void addRelativeHumidityData(double value, long timestamp) {
        addValueElement("hum", value, timestamp);
    }

    /**
     * Masoara temperatura ambientala (camerii) in grade Celsius
     *
     * @param value
     * @param timestamp
     */
    public void addAmbientTemperatureData(double value, long timestamp) {
        addValueElement("temp", value, timestamp);
    }

    /**
     * Adauga un element xml (de orice tip) in fisier.<br/><br/> NU folositi
     * daca nu sunteti siguri ca se va potrivi cu standardul
     *
     * @param e elementul xml
     */
    public void addElement(Element e) {
        synchronized (this) {
            root.appendChild(e);
        }
    }

    private void addCoordElement(String name, double x, double y, double z, long timestamp) {
        try {
            Element e = doc.createElement(name);
            e.setAttribute("x", Double.toString(x));
            e.setAttribute("y", Double.toString(y));
            e.setAttribute("z", Double.toString(z));
            e.setAttribute("time", Long.toString(timestamp));
            addElement(e);
        } catch (NullPointerException ex) {
            throw new NullPointerException("Metoda startCollecting nu a fost apelata");
        }
    }

    private void addValueElement(String name, double value, long timestamp) {
        try {
            Element e = doc.createElement(name);
            e.setAttribute("value", Double.toString(value));
            e.setAttribute("time", Long.toString(timestamp));
            addElement(e);
        } catch (NullPointerException ex) {
            throw new NullPointerException("Metoda startCollecting nu a fost apelata");
        }
    }
}
