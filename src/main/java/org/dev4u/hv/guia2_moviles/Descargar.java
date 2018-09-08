package org.dev4u.hv.guia2_moviles;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by admin on 24/8/17.
 */

class Estado{
    public int estado;
    public String msg;

    public Estado(int estado, String msg) {
        this.estado = estado;
        this.msg = msg;
    }
}

public class Descargar extends AsyncTask<String, Integer, Estado> {
    //estados de la descarga
    private final int OK    = 1;
    private final int ERROR = 0;

    private Button btnDescargar;
    private ProgressBar barra;
    private TextView lblEstado;
    private Context context;
    private String NuevoNombre;
    private Boolean rbnCambioNombre;

    public Descargar(Context context,TextView estado,Button descargar,ProgressBar barra,String nuevonombre,Boolean rbnCambioNombre) {
        this.context      = context;
        this.lblEstado    = estado;
        this.btnDescargar = descargar;
        this.barra = barra;
        this.NuevoNombre = nuevonombre;
        this.rbnCambioNombre = rbnCambioNombre;
    }

    @Override
    protected Estado doInBackground(String... sUrl) {
        InputStream entrada = null;
        OutputStream salida = null;
        HttpURLConnection coneccion = null;
        String ruta = null;
        try {
            //nos conectamos a la primera url que enviamos
            URL url = new URL(sUrl[0]);
            coneccion = (HttpURLConnection) url.openConnection();
            coneccion.connect();

            //obtenemos el nombre del archivo y su extencion, eje : imagen.jpg
            String nombre = url.getFile();

            if(rbnCambioNombre){
            String nuevo =  "/"+NuevoNombre.toString();
                nombre = nuevo.substring(nuevo.lastIndexOf("/")+1,nuevo.length());
            }else{
                nombre = nombre.substring(nombre.lastIndexOf("/")+1,nombre.length());
            }


            // se espera HTTP 200 OK, si no es el resultado lanzamos una excepcion
            // instead of the file
            if (coneccion.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new Exception("Server returned HTTP " + coneccion.getResponseCode()
                        + " " + coneccion.getResponseMessage());
            }

            // esto muestra el tamaño del archivo en porcentaje
            // puede ser -1 si el server no responde o hay error
            int fileLength = coneccion.getContentLength();

            // descargando el archivo
            entrada = coneccion.getInputStream();

            //preparamos la ruta
            //TODO donde dice nombre es el nombre que se guardara el archivo

                ruta = Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        .getAbsolutePath()+ "/"+nombre;



            //si el archivo ya existe lanzamos otra excepcion
            if(new File(ruta).exists())
                throw new Exception("Ya existe un archivo llamado "+nombre);

            //preparamos la salida
            salida = new FileOutputStream(ruta);

            //creamos el buffer y contador para la descarga
            byte data[] = new byte[4096];
            long total = 0;
            int count;
            while ((count = entrada.read(data)) != -1) {
                // si cancela la descarga
                if (isCancelled()) {
                    entrada.close();
                    return null;
                }
                total += count;
                // actualizando el progreso

                         publishProgress(count);/*en porcentaje*/    //solo si el total es mayor a cero



                salida.write(data, 0, count);//escribiendo en el archivo
            }
        } catch (Exception e) {
            return new Estado(ERROR,e.getMessage());//si hay excepciones las retornamos usando la clase estado
        } finally {//finalmente
            try {//cerramos los flujos de entrada y salida
                if (salida != null)
                    salida.close();
                if (entrada != null)
                    entrada.close();
            } catch (IOException ignored) {
            }//cerramos la conexion
            if (coneccion != null)
                coneccion.disconnect();
        }//si todo esta ok retornamos la ruta
        return new Estado(OK,ruta);
    }

    @Override
    protected void onPreExecute() {//esto ocurre antes de descargar
        super.onPreExecute();
        btnDescargar.setText("Descargando...");
        btnDescargar.setEnabled(false);//bloqueamos el boton
        barra.setProgress(0);
        lblEstado.setText("0%");
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {//esto ocurre mientras se descarga
        super.onProgressUpdate(progress);
        //se actualizan los valores
        //el porcentaje es de 0 - 100
        barra.setProgress(progress[0]);
        lblEstado.setText(progress[0]+"%");
    }

    @Override
    protected void onPostExecute(Estado result) {//al finalizar
        btnDescargar.setEnabled(true);//desbloqueamos el boton
        btnDescargar.setText("Descargar");
        if (result.estado==ERROR) {//miramos el estado
            Toast.makeText(context, "Error de descarga: " + result.msg, Toast.LENGTH_LONG).show();
        }else {
            lblEstado.setText("100%");
            Toast.makeText(context, "Archivo descargado en"+result.msg, Toast.LENGTH_LONG).show();
        }
    }

}



/////////////////////////clase para la carga de la barra de progreso///////////////////////////////
 /* class Cargar extends AsyncTask<Void , Integer, String>{
    Context context;
    ProgressBar barra;
    TextView lblValor;
    public Cargar(Context context,ProgressBar barra,TextView lblValor){
        this.context = context;
        this.barra = barra;
        this.lblValor = lblValor;
    }

    @Override
    protected String doInBackground(Void... params) {

        for(Integer contador=0;contador<=100;contador++){
            try{
                Thread.sleep(5);//milsegundos
                publishProgress(contador);
            }catch (InterruptedException e){
                e.printStackTrace();

            }
        }
        return  "Tarea Completa";
    }

    @Override
    protected void onPreExecute() {
        barra.setProgress(0);
        lblValor.setText("0%");
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String s) {
        Toast.makeText(context,"Tarea completada",Toast.LENGTH_SHORT).show();
        super.onPostExecute(s);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        barra.setProgress(values[0]);
        lblValor.setText(values[0]+"%");
        super.onProgressUpdate(values);
    }
}
*/
////////////////////////////////////////////////////////////////////////////////////////////////////
