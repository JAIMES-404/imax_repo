package com.imax.app.managers;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.imax.app.App;
import com.imax.app.R;
import com.imax.app.utils.UnauthorizedException;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;

import okhttp3.ResponseBody;
import retrofit2.Response;

public class DataBaseHelper extends SQLiteAssetHelper {
    public final String TAG = getClass().getName();
    public static final String DATABASE_NAME = "xalestore.db";
    private static final int DATABASE_VERSION = 25;
    public String pdfRespuesta = "";
    public static String [][] pedidos = new String[1000][1000];

    public static int flgIdDetalle = 0;


    private App app;
    private Context context;
    private static DataBaseHelper dbInstance = null;
    public DataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public static DataBaseHelper getInstance(Context ctx) {
        //Application context no es igual a context de un activity
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (dbInstance == null) {
            dbInstance = new DataBaseHelper(ctx.getApplicationContext());
        }
        return dbInstance;
    }

    private DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        // Delete old database when upgrading
        setForcedUpgrade(DATABASE_VERSION);
    }


    public void sincro(Response<ResponseBody> response, String table) throws Exception {
        try {
            if (response.isSuccessful()) {
                String message = "";
                JSONObject jsonObject = new JSONObject(response.body().string());

                if(jsonObject.has("version")){
                     message = jsonObject.getString("version");
                }

                if (jsonObject.has("results")){
                    JSONArray jsonstring = jsonObject.getJSONArray("results");

                    String method = "actualizar_byh_"+table;
                    Log.i(TAG, table + ": " + jsonstring.length() + " registros - "+method);
                    Method helperMethod = getClass().getMethod(method, JSONArray.class);
                    helperMethod.invoke(this, jsonstring);
                    Log.i(TAG, table + " SINCRONIZADO ");
                }else{
                    throw new Exception(message);
                }
            } else {
                JSONObject jsonError;
                String message = "";
                switch (response.code()) {
                    case 401:
                        jsonError = new JSONObject(response.errorBody().string());
                        if(jsonError.has("message")){
                            message = jsonError.getString("message");
                        }
                        throw new UnauthorizedException((message != null) ? message : context.getString(R.string.error_sesion_no_autorizada));
                    case 408:
                        jsonError = new JSONObject(response.errorBody().string());
                        if(jsonError.has("message")){
                            message = jsonError.getString("message");
                        }
                        throw new UnauthorizedException((message != null) ? message : context.getString(R.string.error_sesion_expirada));
                    case 404:
                        throw new Resources.NotFoundException();
                    default:
                        throw new Exception();
                }
            }
        } catch (Resources.NotFoundException e){
            Log.e(TAG, "NO SINCRONIZADO : Recurso no encontrado "+e.getMessage());
            e.printStackTrace();
            throw new Resources.NotFoundException();//atrapa la excepcion pero la vuelve a lanzar (para que la la actividad la vuelva a detectar)
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "NO SINCRONIZADO : No existe el metodo actualizar "+ table +" en DataBaseHelper");
            e.printStackTrace();
            throw new NoSuchMethodException(e.getMessage());//atrapa la excepcion pero la vuelve a lanzar (para que la la actividad la vuelva a detectar)
        } catch (SocketTimeoutException e) {
            //Error relacionado a la webservice
            Log.e(TAG, table + " SOCKETTIMEOUT EXCEPTION :" + e.getMessage());
            e.printStackTrace();
            throw new SocketTimeoutException();
        } catch (IOException e) {
            //Error relacionado a la webservice
            Log.e(TAG, table + " IO EXCEPTION:" + e.getMessage());
            //ex.printStackTrace();
            throw new IOException(e);
        } catch (JSONException e) {
            //Error relacionado a la webservice
            Log.e(TAG, table + " JSON EXCEPTION:" + e.getMessage());
            //ex.printStackTrace();
            throw new RuntimeException(e);
        } catch (UnauthorizedException e){
            Log.e(TAG, table + " UNAUTHORIZED EXCEPTION: "+e.getMessage());
            throw new UnauthorizedException(e.getMessage());
        } catch (Exception e) {
            //Error relacionado a la webservice
            if (e.getCause() != null) {
                Log.e(TAG, table + " GENERAL EXCEPTION CAUSE:" + e.getCause().getMessage());
                throw new Exception(e.getCause());
            }else {
                Log.e(TAG, table + " GENERAL EXCEPTION:" + e.getMessage());
                throw new Exception(e);
            }
        }
    }


    public void sincronizarPedido(Response<ResponseBody> response) throws Exception {
        String table = "xms_order";

        try {
            if (response.isSuccessful()) {
                String message = "";
                JSONObject jsonObject = new JSONObject(response.body().string());

                if (jsonObject.has("data")){
                    JSONArray jsonstring = jsonObject.getJSONArray("data");

                    String method = "actualizar_byh_"+table;
                    Log.i(TAG, table + ": " + jsonstring.length() + " registros - "+method);
                    actualizar_byh_xms_order(jsonstring);
                    Log.i(TAG, table + " SINCRONIZADO ");
                }else{
                    throw new Exception(message);
                }
            } else {
                JSONObject jsonError;
                String message = "";
                switch (response.code()) {
                    case 401:
                        jsonError = new JSONObject(response.errorBody().string());
                        if(jsonError.has("message")){
                            message = jsonError.getString("message");
                        }
                        throw new UnauthorizedException((message != null) ? message : context.getString(R.string.error_sesion_no_autorizada));
                    case 408:
                        jsonError = new JSONObject(response.errorBody().string());
                        if(jsonError.has("message")){
                            message = jsonError.getString("message");
                        }
                        throw new UnauthorizedException((message != null) ? message : context.getString(R.string.error_sesion_expirada));
                    case 404:
                        throw new Resources.NotFoundException();
                    default:
                        throw new Exception();
                }
            }
        } catch (Resources.NotFoundException e){
            Log.e(TAG, "NO SINCRONIZADO : Recurso no encontrado "+e.getMessage());
            e.printStackTrace();
            throw new Resources.NotFoundException();//atrapa la excepcion pero la vuelve a lanzar (para que la la actividad la vuelva a detectar)
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "NO SINCRONIZADO : No existe el metodo actualizar "+ table +" en DataBaseHelper");
            e.printStackTrace();
            throw new NoSuchMethodException(e.getMessage());//atrapa la excepcion pero la vuelve a lanzar (para que la la actividad la vuelva a detectar)
        } catch (SocketTimeoutException e) {
            //Error relacionado a la webservice
            Log.e(TAG, table + " SOCKETTIMEOUT EXCEPTION :" + e.getMessage());
            e.printStackTrace();
            throw new SocketTimeoutException();
        } catch (IOException e) {
            //Error relacionado a la webservice
            Log.e(TAG, table + " IO EXCEPTION:" + e.getMessage());
            //ex.printStackTrace();
            throw new IOException(e);
        } catch (JSONException e) {
            //Error relacionado a la webservice
            Log.e(TAG, table + " JSON EXCEPTION:" + e.getMessage());
            //ex.printStackTrace();
            throw new RuntimeException(e);
        } catch (UnauthorizedException e){
            Log.e(TAG, table + " UNAUTHORIZED EXCEPTION: "+e.getMessage());
            throw new UnauthorizedException(e.getMessage());
        } catch (Exception e) {
            //Error relacionado a la webservice
            if (e.getCause() != null) {
                Log.e(TAG, table + " GENERAL EXCEPTION CAUSE:" + e.getCause().getMessage());
                throw new Exception(e.getCause());
            }else {
                Log.e(TAG, table + " GENERAL EXCEPTION:" + e.getMessage());
                throw new Exception(e);
            }
        }
    }


    public void sincroObject(Response<ResponseBody> response, String table) throws Exception {
        try {
            if (response.isSuccessful()) {
                String message = "";
                JSONObject jsonObject = new JSONObject(response.body().string());
                boolean success = jsonObject.getBoolean("success");
                String data = jsonObject.getString("data");
                if(jsonObject.has("message")){
                    message = jsonObject.getString("message");
                }
                if (success){
                    JSONObject jsonstring = new JSONObject(data);
                    String method = "actualizar_"+table;
                    Log.i(TAG, table + ": " + jsonstring.length() + " registros - "+method);
                    Method helperMethod = getClass().getMethod(method, JSONObject.class);
                    helperMethod.invoke(this, jsonstring);
                    Log.i(TAG, table + " SINCRONIZADO ");
                }else{
                    throw new Exception(message);
                }
            } else {
                switch (response.code()) {
                    case 401:
                        throw new UnauthorizedException("401 Unauthorized");
                    case 404:
                        throw new Resources.NotFoundException();
                    case 500:
                        throw new Exception();
                    default:
                        throw new Exception();
                }
            }
        } catch (Resources.NotFoundException e){
            Log.e(TAG, "NO SINCRONIZADO : Recurso no encontrado "+e.getMessage());
            e.printStackTrace();
            throw new Resources.NotFoundException();//atrapa la excepcion pero la vuelve a lanzar (para que la la actividad la vuelva a detectar)
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "NO SINCRONIZADO : No existe el metodo actualizar_"+ table +" en DataBaseHelper");
            e.printStackTrace();
            throw new NoSuchMethodException();//atrapa la excepcion pero la vuelve a lanzar (para que la la actividad la vuelva a detectar)
        } catch (SocketTimeoutException e) {
            //Error relacionado a la webservice
            Log.e(TAG, table + " SOCKETTIMEOUT EXCEPTION :" + e.getMessage());
            e.printStackTrace();
            throw new SocketTimeoutException();
        } catch (IOException e) {
            //Error relacionado a la webservice
            Log.e(TAG, table + " IO EXCEPTION:" + e.getMessage());
            //ex.printStackTrace();
            throw new IOException(e);
        } catch (JSONException e) {
            //Error relacionado a la webservice
            Log.e(TAG, table + " JSON EXCEPTION:" + e.getMessage());
            //ex.printStackTrace();
            throw new RuntimeException(e);
        } catch (UnauthorizedException e){
            Log.e(TAG, table + " UNAUTHORIZED EXCEPTION: "+e.getMessage());
            throw new UnauthorizedException(e.getMessage());
        } catch (Exception e) {
            //Error relacionado a la webservice
            if (e.getCause() != null) {
                Log.e(TAG, table + " GENERAL EXCEPTION CAUSE:" + e.getCause().getMessage());
                throw new Exception(e.getCause());
            }else {
                Log.e(TAG, table + " GENERAL EXCEPTION:" + e.getMessage());
                throw new Exception(e);
            }
        }
    }

    /** Sincronización de tablas **/

    public void actualizar_byh_xms_order(JSONArray jArray) {
        JSONObject jsonData = null;

        ContentValues cv = new ContentValues();

        String table = TablesHelper.xms_order.table;

        String id_numero = TablesHelper.xms_order.id_numero;
        String fecha = TablesHelper.xms_order.fecha;
        String fechaDeVenc = TablesHelper.xms_order.fechaDeVenc;
        String fechaDeEntrega = TablesHelper.xms_order.fechaDeEntrega;
        String id_cliente = TablesHelper.xms_order.id_cliente;
        String direcc = TablesHelper.xms_order.direcc;
        String id_cond = TablesHelper.xms_order.id_cond;
        String id_vendedor = TablesHelper.xms_order.id_vendedor;
        String id_cobrador = TablesHelper.xms_order.id_cobrador;
        String id_almacen = TablesHelper.xms_order.id_almacen;
        String moneda = TablesHelper.xms_order.moneda;
        String tipodecambio = TablesHelper.xms_order.tipodecambio;
        String subtotal = TablesHelper.xms_order.subtotal;
        String descuento = TablesHelper.xms_order.descuento;
        String total = TablesHelper.xms_order.total;
        String id_usuario = TablesHelper.xms_order.id_usuario;
        String id_distrito = TablesHelper.xms_order.id_distrito;
        String codubigeo = TablesHelper.xms_order.codubigeo;
        String total_opgratuito = TablesHelper.xms_order.total_opgratuito;
        String total_opexonerado = TablesHelper.xms_order.total_opexonerado;


        String table_detalle = TablesHelper.xms_order_detail.table;

        String id_numero_detalle = TablesHelper.xms_order_detail.id_numero;
        String id_producto_detalle = TablesHelper.xms_order_detail.id_producto;
        String moneda_detalle = TablesHelper.xms_order_detail.moneda;
        String tipoDeCambio_detalle = TablesHelper.xms_order_detail.tipoDeCambio;
        String precioUnit_detalle = TablesHelper.xms_order_detail.precioUnit;
        String cantidad = TablesHelper.xms_order_detail.cantidad;
        String monto = TablesHelper.xms_order_detail.monto;
        String precioUnitAlTipCam = TablesHelper.xms_order_detail.precioUnitAlTipCam;
        String montoAlTipCam = TablesHelper.xms_order_detail.montoAlTipCam;
        String cantidad2 = TablesHelper.xms_order_detail.cantidad2;
        String tipotributo = TablesHelper.xms_order_detail.tipotributo;
        String bonificacion = TablesHelper.xms_order_detail.bonificacion;
        String precunitgrat = TablesHelper.xms_order_detail.precunitgrat;
        String montograt = TablesHelper.xms_order_detail.montograt;

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        try {
            db.delete(table, null, null);
            db.delete(table_detalle, null, null);

            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);

                cv.put(id_numero, jsonData.getString("idNumero"));
                cv.put(fecha, jsonData.getString("fecha"));
                cv.put(fechaDeVenc, jsonData.getString("fechaDeVenc"));
                cv.put(fechaDeEntrega, jsonData.getString("fechaDeEntrega"));
                cv.put(id_cliente, jsonData.getString("idCliente"));
                cv.put(direcc, jsonData.getString("direccion"));
                cv.put(id_cond, jsonData.getString("idCond"));
                cv.put(id_vendedor, jsonData.getString("idVendedor"));
                cv.put(id_cobrador, jsonData.optInt("idCobrador", -1));
                cv.put(id_almacen, jsonData.getString("idAlmacen"));
                cv.put(moneda, jsonData.getString("moneda"));
                cv.put(tipodecambio, jsonData.getString("tipoDeCambio"));
                cv.put(subtotal, jsonData.getString("subtotal"));
                cv.put(descuento, jsonData.getString("descuento"));
                cv.put(total, jsonData.getString("total"));
                cv.put(id_usuario, jsonData.getString("idUsuario"));
                cv.put(id_distrito, jsonData.getString("idDistrito"));
                cv.put(codubigeo, jsonData.getString("codUbigeo"));
                cv.put(total_opgratuito, jsonData.getString("total_opgratuito"));
                cv.put(total_opexonerado, jsonData.getString("total_opexonerado"));

                if(jsonData.has("ordenPedidoDetalleList")){
                    JSONArray jsonArrayDetalle = jsonData.getJSONArray("ordenPedidoDetalleList");

                    for (int j = 0; j < jsonArrayDetalle.length(); j++) {
                        ContentValues cv_detalle = new ContentValues();

                        JSONObject jsonDataDetalle = jsonArrayDetalle.getJSONObject(j);

                        SQLiteDatabase db_detalle = getWritableDatabase();
                        db_detalle.beginTransaction();

                        try {

                            cv_detalle.put(id_numero_detalle, jsonDataDetalle.getString("idNumero"));
                            cv_detalle.put(id_producto_detalle, jsonDataDetalle.getString("idProducto"));
                            cv_detalle.put(moneda_detalle, jsonDataDetalle.getString("moneda"));
                            cv_detalle.put(tipoDeCambio_detalle, jsonDataDetalle.getString("tipoDeCambio"));
                            cv_detalle.put(precioUnit_detalle, jsonDataDetalle.getString("precioUnit"));
                            cv_detalle.put(cantidad, jsonDataDetalle.getString("cantidad"));
                            cv_detalle.put(monto, jsonDataDetalle.getString("monto"));
                            cv_detalle.put(precioUnitAlTipCam, jsonDataDetalle.getString("precioUnitAlTipCam"));
                            cv_detalle.put(montoAlTipCam, jsonDataDetalle.getString("montoAlTipCam"));
                            cv_detalle.put(cantidad2, jsonDataDetalle.getString("cantidad2"));
                            cv_detalle.put(tipotributo, jsonDataDetalle.getString("tipotributo"));
                            cv_detalle.put(bonificacion, jsonDataDetalle.getString("bonificacion"));
                            cv_detalle.put(precunitgrat, jsonDataDetalle.getString("precunitgrat"));
                            cv_detalle.put(montograt, jsonDataDetalle.getString("montograt"));

                            db_detalle.insertOrThrow(table_detalle, null, cv_detalle);
                            db_detalle.setTransactionSuccessful();

                        } catch (JSONException e) {
                            Log.e(TAG, table_detalle + ":" + e.getMessage());
                            throw new RuntimeException(e);
                        } finally {
                            db_detalle.endTransaction();
                        }
                        
                    }
                }

                db.insertOrThrow(table, null, cv);
            }
            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }


    public void actualizar_byh_xms_client(JSONArray jArray) {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.xms_client.table;

        String idCliente = TablesHelper.xms_client.id_cliente;
        String nombre = TablesHelper.xms_client.nombre;
        String representante = TablesHelper.xms_client.representante;
        String domicilio = TablesHelper.xms_client.domicilio;
        String idDistrito = TablesHelper.xms_client.id_distrito;
        String local = TablesHelper.xms_client.local;
        String ocupacion = TablesHelper.xms_client.ocupacion;
        String telefonos = TablesHelper.xms_client.telefonos;
        String fax = TablesHelper.xms_client.fax;
        String ruc = TablesHelper.xms_client.ruc;
        String dni = TablesHelper.xms_client.dni;
        String moneda = TablesHelper.xms_client.moneda;
        String idTienda = TablesHelper.xms_client.id_tienda;
        String idVendedor = TablesHelper.xms_client.id_vendedor;
        String idCobrador = TablesHelper.xms_client.id_cobrador;
        String activo = TablesHelper.xms_client.activo;
        String direcc1 = TablesHelper.xms_client.direcc1;
        String direcc2 = TablesHelper.xms_client.direcc2;
        String direcc3 = TablesHelper.xms_client.direcc3;
        String direcc4 = TablesHelper.xms_client.direcc4;
        String idDistritoDirecc1 = TablesHelper.xms_client.id_distrito_direcc1;
        String idDistritoDirecc2 = TablesHelper.xms_client.id_distrito_direcc2;
        String idDistritoDirecc3 = TablesHelper.xms_client.id_distrito_direcc3;
        String idDistritoDirecc4 = TablesHelper.xms_client.id_distrito_direcc4;
        String email = TablesHelper.xms_client.email;
        String telefono2 = TablesHelper.xms_client.telefono2;
        String telefono3 = TablesHelper.xms_client.telefono3;
        String telefono4 = TablesHelper.xms_client.telefono4;
        String codUbigeo = TablesHelper.xms_client.codubigeo;
        String email1 = TablesHelper.xms_client.email1;
        String email2 = TablesHelper.xms_client.email2;
        String email3 = TablesHelper.xms_client.email3;
        String email4 = TablesHelper.xms_client.email4;
        String codUbigeo1 = TablesHelper.xms_client.codubigeo1;
        String codUbigeo2 = TablesHelper.xms_client.codubigeo2;
        String codUbigeo3 = TablesHelper.xms_client.codubigeo3;
        String codUbigeo4 = TablesHelper.xms_client.codubigeo4;
        String nacionalidad = TablesHelper.xms_client.nacionalidad;
        String carnetExtranjeria = TablesHelper.xms_client.carnet_extranjeria;

        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            db.delete(table, null, null);
            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);

                cv.put(idCliente, jsonData.optInt("idCliente", -1));
                cv.put(nombre, jsonData.getString("nombre"));
                cv.put(representante, jsonData.getString("representante"));
                cv.put(domicilio, jsonData.getString("domicilio"));
                cv.put(idDistrito, jsonData.optInt("idDistrito", -1));
                cv.put(local, jsonData.optInt("local", -1));
                cv.put(ocupacion, jsonData.getString("ocupacion"));
                cv.put(telefonos, jsonData.getString("telefonos"));
                cv.put(fax, jsonData.getString("fax"));
                cv.put(ruc, jsonData.getString("ruc"));
                cv.put(dni, jsonData.getString("dni"));
                cv.put(moneda, jsonData.getString("moneda"));
                cv.put(idTienda, jsonData.optInt("idTienda", -1));
                cv.put(idVendedor, jsonData.optInt("idVendedor", -1));
                cv.put(idCobrador, jsonData.optInt("idCobrador", -1));
                cv.put(activo, jsonData.optInt("activo", -1));
                cv.put(direcc1, jsonData.getString("direcc1"));
                cv.put(direcc2, jsonData.getString("direcc2"));
                cv.put(direcc3, jsonData.getString("direcc3"));
                cv.put(direcc4, jsonData.getString("direcc4"));
                cv.put(idDistritoDirecc1, jsonData.optInt("idDistritoDirecc1", -1));
                cv.put(idDistritoDirecc2, jsonData.optInt("idDistritoDirecc2", -1));
                cv.put(idDistritoDirecc3, jsonData.optInt("idDistritoDirecc3", -1));
                cv.put(idDistritoDirecc4, jsonData.optInt("idDistritoDirecc4", -1));
                cv.put(email, jsonData.getString("email"));
                cv.put(telefono2, jsonData.getString("telefono2"));
                cv.put(telefono3, jsonData.getString("telefono3"));
                cv.put(telefono4, jsonData.getString("telefono4"));
                cv.put(codUbigeo, jsonData.getString("codUbigeo"));
                cv.put(email1, jsonData.getString("email1"));
                cv.put(email2, jsonData.getString("email2"));
                cv.put(email3, jsonData.getString("email3"));
                cv.put(email4, jsonData.getString("email4"));
                cv.put(codUbigeo1, jsonData.getString("codUbigeo1"));
                cv.put(codUbigeo2, jsonData.getString("codUbigeo2"));
                cv.put(codUbigeo3, jsonData.getString("codUbigeo3"));
                cv.put(codUbigeo4, jsonData.getString("codUbigeo4"));
                cv.put(nacionalidad, jsonData.getString("nacionalidad"));
                cv.put(carnetExtranjeria, jsonData.getString("carnetExtranjeria"));

                db.insertOrThrow(table, null, cv);
            }
            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizar_byh_xms_product(JSONArray jArray) {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.xms_product.table;

        String id_producto = TablesHelper.xms_product.id_producto;
        String nombre = TablesHelper.xms_product.nombre;
        String facuni = TablesHelper.xms_product.facuni;
        String id_medida = TablesHelper.xms_product.id_medida;
        String id_unidad = TablesHelper.xms_product.id_unidad;
        String moneda = TablesHelper.xms_product.moneda;
        String kardex = TablesHelper.xms_product.kardex;
        String series = TablesHelper.xms_product.series;
        String stockmin = TablesHelper.xms_product.stockmin;
        String stockmax = TablesHelper.xms_product.stockmax;
        String prevtaigv = TablesHelper.xms_product.prevtaigv;
        String preciovtamen = TablesHelper.xms_product.preciovtamen;
        String preciovtamay = TablesHelper.xms_product.preciovtamay;
        String mone_cost = TablesHelper.xms_product.mone_cost;
        String costo = TablesHelper.xms_product.costo;
        String peso = TablesHelper.xms_product.peso;
        String codigo = TablesHelper.xms_product.codigo;
        String unidad = TablesHelper.xms_product.unidad;
        String id_marca = TablesHelper.xms_product.id_marca;
        String tipo_atributo = TablesHelper.xms_product.tipo_atributo;

        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            db.delete(table, null, null);
            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);

                cv.put(id_producto, jsonData.getString("id_producto"));
                cv.put(nombre, jsonData.getString("nombre"));
                cv.put(facuni, jsonData.getString("facuni"));
                cv.put(id_medida, jsonData.getString("id_medida"));
                cv.put(id_unidad, jsonData.getString("id_unidad"));
                cv.put(moneda, jsonData.getString("moneda"));
                cv.put(kardex, jsonData.getString("kardex"));
                cv.put(series, jsonData.getString("series"));
                cv.put(stockmin, jsonData.getString("stockmin"));
                cv.put(stockmax, jsonData.getString("stockmax"));
                cv.put(prevtaigv, jsonData.getString("prevtaigv"));
                cv.put(preciovtamen, jsonData.getString("preciovtamen"));
                cv.put(preciovtamay, jsonData.getString("preciovtamay"));
                cv.put(mone_cost, jsonData.getString("mone_cost"));
                cv.put(costo, jsonData.getString("costo"));
                cv.put(peso, jsonData.getString("peso"));
                cv.put(codigo, jsonData.getString("codigo"));
                cv.put(unidad, jsonData.getString("unidad"));
                cv.put(id_marca, jsonData.getString("id_marca"));
                cv.put(tipo_atributo, jsonData.getString("tipotributo"));

                db.insertOrThrow(table, null, cv);
            }
            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizar_byh_xms_almacenes(JSONArray jArray) {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();

        String table = TablesHelper.xms_almacenes.table;

        String id = TablesHelper.xms_almacenes.id_almacen;
        String nombre = TablesHelper.xms_almacenes.nombre;
        String descripcion = TablesHelper.xms_almacenes.descripcion;

        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            db.delete(table, null, null);
            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);

                cv.put(id, jsonData.getString("id_almacen"));
                cv.put(nombre, jsonData.getString("nombre"));
                cv.put(descripcion, jsonData.getString("ubicacion"));

                db.insertOrThrow(table, null, cv);
            }
            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizar_byh_xms_condiciones(JSONArray jArray) {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.xms_condiciones.table;

        String id_condicion = TablesHelper.xms_condiciones.id_condicion;
        String nombre = TablesHelper.xms_condiciones.nombre;
        String dias = TablesHelper.xms_condiciones.dias;
        String tipo = TablesHelper.xms_condiciones.tipo;

        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            db.delete(table, null, null);
            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);

                cv.put(id_condicion, jsonData.getString("id_condicion"));
                cv.put(nombre, jsonData.getString("nombre"));
                cv.put(dias, jsonData.getString("dias"));
                cv.put(tipo, jsonData.getString("tipo"));

                db.insertOrThrow(table, null, cv);
            }
            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizar_byh_xms_distritos(JSONArray jArray) {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.xms_distritos.table;

        String id_distrito = TablesHelper.xms_distritos.id_distrito;
        String nombre = TablesHelper.xms_distritos.nombre;
        String id_provincia = TablesHelper.xms_distritos.id_provincia;
        String cont_distsunat = TablesHelper.xms_distritos.cont_distsunat;
        String codubigeo = TablesHelper.xms_distritos.codubigeo;

        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            db.delete(table, null, null);
            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(id_distrito, jsonData.getString("id_distrito"));
                cv.put(nombre, jsonData.getString("nombre"));
                cv.put(id_provincia, jsonData.getString("id_provincia"));
                cv.put(cont_distsunat, jsonData.getString("cont_distsunat"));
                cv.put(codubigeo, jsonData.getString("codubigeo"));

                db.insertOrThrow(table, null, cv);
            }
            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizar_byh_xms_personal(JSONArray jArray) {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.xms_personal.table;

        String id_personal = TablesHelper.xms_personal.id_personal;
        String nombre = TablesHelper.xms_personal.nombre;
        String domicilio = TablesHelper.xms_personal.domicilio;
        String id_distrito = TablesHelper.xms_personal.id_distrito;
        String telefonos = TablesHelper.xms_personal.telefonos;
        String dni = TablesHelper.xms_personal.dni;
        String ruc = TablesHelper.xms_personal.ruc;
        String pcomision = TablesHelper.xms_personal.pcomision;
        String id_ocupacion = TablesHelper.xms_personal.id_ocupacion;
        String fechanac = TablesHelper.xms_personal.fechanac;
        String sexo = TablesHelper.xms_personal.sexo;
        String activo = TablesHelper.xms_personal.activo;

        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            db.delete(table, null, null);
            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(id_personal, jsonData.getString("id_personal"));
                cv.put(nombre, jsonData.getString("nombre"));
                cv.put(domicilio, jsonData.getString("domicilio"));
                cv.put(id_distrito, jsonData.getString("id_distrito"));
                cv.put(telefonos, jsonData.getString("telefonos"));
                cv.put(dni, jsonData.getString("dni"));
                cv.put(ruc, jsonData.getString("ruc"));
                cv.put(pcomision, jsonData.getString("pcomision"));
                cv.put(id_ocupacion, jsonData.getString("id_ocupacion"));
                cv.put(fechanac, jsonData.getString("fechanac"));
                cv.put(sexo, jsonData.getString("sexo"));
                cv.put(activo, jsonData.getString("activo"));

                db.insertOrThrow(table, null, cv);
            }
            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizar_byh_xms_tcambio(JSONArray jArray) {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.xms_tcambio.table;
        String fecha = TablesHelper.xms_tcambio.fecha;
        String libre = TablesHelper.xms_tcambio.libre;
        String compra = TablesHelper.xms_tcambio.compra;
        String venta = TablesHelper.xms_tcambio.venta;

        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            db.delete(table, null, null);
            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(fecha, jsonData.getString("fecha"));
                cv.put(libre, jsonData.getString("libre"));
                cv.put(compra, jsonData.getString("compra"));
                cv.put(venta, jsonData.getString("venta"));

                db.insertOrThrow(table, null, cv);
            }
            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizar_byh_xms_unidad_medida(JSONArray jArray) {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.xms_unidad_medida.table;

        String id = TablesHelper.xms_unidad_medida.id;
        String descripcion = TablesHelper.xms_unidad_medida.descripcion;
        String comercial = TablesHelper.xms_unidad_medida.comercial;
        String sunat = TablesHelper.xms_unidad_medida.sunat;

        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            db.delete(table, null, null);
            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(id, jsonData.getString("id_unidad"));
                cv.put(descripcion, jsonData.getString("nombre"));
                cv.put(comercial, jsonData.getString("nombre"));
                cv.put(sunat, jsonData.getString("nombre"));

                db.insertOrThrow(table, null, cv);
            }
            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizar_byh_xms_medidas(JSONArray jArray) {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();
        String table = TablesHelper.xms_medidas.table;

        String id = TablesHelper.xms_medidas.id_medida;
        String nombre = TablesHelper.xms_medidas.nombre;
        String codigo = TablesHelper.xms_medidas.codigo;

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        try {
            db.delete(table, null, null);
            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(id, jsonData.getString("idMedida"));
                cv.put(nombre, jsonData.getString("nombre"));
                cv.put(codigo, jsonData.getString("codigo"));

                db.insertOrThrow(table, null, cv);
            }
            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizar_byh_xms_marcas(JSONArray jArray) {
        JSONObject jsonData = null;
        ContentValues cv = new ContentValues();

        String table = TablesHelper.xms_marcas.table;
        String id = TablesHelper.xms_marcas.id;
        String descripcion = TablesHelper.xms_marcas.descripcion;

        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            db.delete(table, null, null);
            for (int i = 0; i < jArray.length(); i++) {
                jsonData = jArray.getJSONObject(i);
                cv.put(id, jsonData.getString("id_marca"));
                cv.put(descripcion, jsonData.getString("nombre"));

                db.insertOrThrow(table, null, cv);
            }
            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void actualizar_byh_xms_asignacion(JSONArray jArray) {
        ContentValues cv = new ContentValues();

        String table = TablesHelper.xms_asignacion.table;

        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            db.delete(table, null, null);
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject jsonData = jArray.getJSONObject(i);

                cv.put("id", jsonData.optInt("id", 1));
                cv.put("address", jsonData.optString("address", ""));
                cv.put("custom_field_1", jsonData.optString("custom_field_1", ""));
                cv.put("custom_field_19", jsonData.optString("custom_field_19", ""));
                cv.put("custom_field_2", jsonData.optString("custom_field_2", ""));
                cv.put("custom_field_3", jsonData.optString("custom_field_3", ""));
                cv.put("inspection_date", jsonData.optString("inspection_date", ""));
                cv.put("name", jsonData.optString("name", ""));
                cv.put("number", jsonData.optString("number", ""));
                cv.put("partner_latitude", jsonData.optDouble("partner_latitude", 0.0));
                cv.put("partner_longitude", jsonData.optDouble("partner_longitude", 0.0));

                if (jsonData.has("coordinator_id") && !jsonData.isNull("coordinator_id")) {
                    JSONArray coordinatorArray = jsonData.getJSONArray("coordinator_id");
                    if (coordinatorArray.length() > 1) {
                        cv.put("coordinator_id", coordinatorArray.getInt(0)); // ID
                        cv.put("coordinator_name", coordinatorArray.getString(1)); // Descripción
                    }
                }

                if (jsonData.has("inspector_id") && !jsonData.isNull("inspector_id")) {
                    JSONArray inspectorArray = jsonData.getJSONArray("inspector_id");
                    if (inspectorArray.length() > 1) {
                        cv.put("inspector_id", inspectorArray.getInt(0)); // ID
                        cv.put("inspector_name", inspectorArray.getString(1)); // Descripción
                    }
                }

                if (jsonData.has("motive_id") && !jsonData.isNull("motive_id")) {
                    if (jsonData.get("motive_id") instanceof JSONArray) {
                        JSONArray motiveArray = jsonData.getJSONArray("motive_id");
                        if (motiveArray.length() > 1) {
                            cv.put("motive_id", motiveArray.getInt(0));
                            cv.put("motive_name", motiveArray.getString(1));
                        }
                    } else if (jsonData.get("motive_id") instanceof Boolean) {
                        boolean motiveBool = jsonData.getBoolean("motive_id");
                        if (!motiveBool) {
                            cv.put("motive_id", 0);
                            cv.put("motive_name", "Sin motivo");
                        }
                    }
                }

                if (jsonData.has("type_id") && !jsonData.isNull("type_id")) {
                    JSONArray typeArray = jsonData.getJSONArray("type_id");
                    if (typeArray.length() > 1) {
                        cv.put("type_id", typeArray.getInt(0));
                        cv.put("type_name", typeArray.getString(1));
                    }
                }
                db.insertOrThrow(table, null, cv);
            }

            db.setTransactionSuccessful();
            Log.i(TAG, table + ": BD Actualizada");
        } catch (JSONException e) {
            Log.e(TAG, table + ":" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }
}
