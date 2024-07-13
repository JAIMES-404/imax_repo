package com.sales.storeapp.ui.activity;

import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.sales.storeapp.App;
import com.sales.storeapp.R;
import com.sales.storeapp.data.api.XMSApi;
import com.sales.storeapp.data.dao.DAOExtras;
import com.sales.storeapp.data.dao.DAOProducto;
import com.sales.storeapp.managers.DataBaseHelper;
import com.sales.storeapp.managers.TablesHelper;
import com.sales.storeapp.ui.login.LoginActivity;
import com.sales.storeapp.utils.Constants;
import com.sales.storeapp.utils.MyDetailDialog;
import com.sales.storeapp.utils.UnauthorizedException;
import com.sales.storeapp.utils.Util;

import java.io.IOException;
import java.net.SocketTimeoutException;

import okhttp3.ResponseBody;
import retrofit2.Response;

public class SplashActivity extends AppCompatActivity {
    public final String TAG = getClass().getName();
    private ImageView img_icon;
    DataBaseHelper dataBaseHelper;
    private DAOProducto daoProducto;
    DAOExtras daoExtras;
    private App app;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        app = (App) getApplicationContext();

        dataBaseHelper = DataBaseHelper.getInstance(this);
        daoProducto = new DAOProducto(getApplicationContext());
        daoExtras = new DAOExtras(getApplicationContext());

        img_icon = findViewById(R.id.img_icon);

        animar();
        init();
    }



    private void init() {
        if (app.isPref_sessionOpen() && daoExtras.existeUsuario()) {
            new async_sincronizacion().execute();
            goToMenuPrincipal();
        }else{
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    private void animar() {
        Animation animationScale = AnimationUtils.loadAnimation(this, R.anim.fade_scale_infinite);
        img_icon.startAnimation(animationScale);

        /*new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SplashActivity.this, "POST", Toast.LENGTH_SHORT).show();
                img_icon.clearAnimation();        
            }
        }, 5000);*/
        
    }

    public void onUserLoggedOut() {
        Log.e(TAG, "RE LOGIN !!!!");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SplashActivity.this, "RE LOGIN !!!!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    class async_sincronizacion extends AsyncTask<Void, String, String> {
        String message;
        protected void onPreExecute() {}

        @Override
        protected String doInBackground(Void... voids) {
            try {
                if (Util.isConnectingToRed(getApplicationContext())) {
                    //Util.backupdDatabase(getApplicationContext());
                    Log.d(TAG,"sincronizando datos...");
                    Response<ResponseBody> response;

                    response = XMSApi.getApiEasyfact(getApplicationContext()).getClientes().execute();
                    dataBaseHelper.sincro(response, TablesHelper.xms_client.table);

                    response = XMSApi.getApiEasyfact(getApplicationContext()).getProductos().execute();
                    dataBaseHelper.sincro(response,TablesHelper.xms_product.table);

                    response = XMSApi.getApiEasyfact(getApplicationContext()).getAlmacenes().execute();
                    dataBaseHelper.sincro(response,TablesHelper.xms_almacenes.table);

                    response = XMSApi.getApiEasyfact(getApplicationContext()).getCondiciones().execute();
                    dataBaseHelper.sincro(response,TablesHelper.xms_condiciones.table);

                    response = XMSApi.getApiEasyfact(getApplicationContext()).getDistritos().execute();
                    dataBaseHelper.sincro(response,TablesHelper.xms_distritos.table);

                    response = XMSApi.getApiEasyfact(getApplicationContext()).getPersonal().execute();
                    dataBaseHelper.sincro(response,TablesHelper.xms_personal.table);

                    response = XMSApi.getApiEasyfact(getApplicationContext()).getTCambio().execute();
                    dataBaseHelper.sincro(response,TablesHelper.xms_tcambio.table);

                    response = XMSApi.getApiEasyfact(getApplicationContext()).getMarcas().execute();
                    dataBaseHelper.sincro(response,TablesHelper.xms_marcas.table);

                    response = XMSApi.getApiEasyfact(getApplicationContext()).getUnidadMedida().execute();
                    dataBaseHelper.sincro(response,TablesHelper.xms_unidad_medida.table);

                   /*

                    response = XMSApi.getApiEasyfact(getApplicationContext()).getLogoEmpresa(app.getPref_idBodegaCliente(), app.getPref_idBodegaCliente()).execute();
                    dataBaseHelper.saveLogoAPK(response, TablesHelper.xms_cliente.table);

                    response = XMSApi.getApiEasyfact(getApplicationContext()).
                            getListaPrecios(app.getPref_idBodegaCliente()).execute();
                    dataBaseHelper.sincro(response,TablesHelper.xms_lista_precio.table);

                    Date c = Calendar.getInstance().getTime();
                    Date tomorrow = new Date(c.getTime() + (1000 * 60 * 60 * 24));
                    Date yesterday = new Date(c.getTime() - (1000 * 60 * 60 * 24));

                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                    String hoy = df.format(c);
                    String mñn = df.format(tomorrow);
                    String ayer = df.format(yesterday);


                    response = XMSApi.getApiEasyfact(getApplicationContext()).
                            getListaPedidoGeneral(app.getPref_idBodegaCliente(),
                                    false, hoy, mñn, true).execute();
                    dataBaseHelper.sincro(response,TablesHelper.xms_pedido_cabecera.table);

                    String[][] pedidosActual;

                    pedidosActual = DataBaseHelper.pedidos;
                    int total =  DataBaseHelper.cantidadPedidos;

                    for (int i = 0; i < total; i++) {

                       response = XMSApi.getApiEasyfact(getApplicationContext()).
                                getPDFPedido(pedidosActual[i][3], "PDF").execute();

                        dataBaseHelper.sincroPDFpedido(response);

                        response = XMSApi.getApiEasyfact(getApplicationContext()).
                                    getListaPedidoDetallado(pedidosActual[i][0],
                                            pedidosActual[i][1],
                                            pedidosActual[i][2],
                                            pedidosActual[i][4],
                                            1).execute();

                        dataBaseHelper.sincroPedidoFacturaBoleta(pedidosActual[i][5], pedidosActual[i][6], pedidosActual[i][7], pedidosActual[i][8],
                                pedidosActual[i][9], pedidosActual[i][10], response, TablesHelper.xms_pedido_detalle.table, TablesHelper.xms_pedido_cabecera.table);
                    }
                */

                   /* List<ListaPrecioModel> listaPrecioModels = daoProducto.getListaPrecios();
                    daoProducto.eliminarListaPrecioProductos();
                    if (listaPrecioModels.size() > 0) {
                        for (ListaPrecioModel listaPrecioModel : listaPrecioModels){
                        response = XMSApi.getApiEasyfact(getApplicationContext()).getListaPrecio(listaPrecioModel.getId(), app.getPref_idBodegaCliente()).execute();
                        dataBaseHelper.sincroListaPrecioProductos(response,TablesHelper.xms_lista_precio_producto.table);
                            }
                        }
                        */


                    /*response = XMSApi.getApiInterface(getApplicationContext()).getBodega().execute();
                    dataBaseHelper.sincroObject(response, TablesHelper.xms_bodega.table);//DONE

                    response = XMSApi.getApiInterface(getApplicationContext()).getProductos().execute();
                    dataBaseHelper.sincro(response, TablesHelper.xms_producto.table);//DONE

                    response = XMSApi.getApiInterface(getApplicationContext()).getPedidos().execute();
                    dataBaseHelper.sincro(response, TablesHelper.xms_pedido_cabecera.table);

                    response = XMSApi.getApiInterface(getApplicationContext()).getMarcas().execute();
                    dataBaseHelper.sincro(response, TablesHelper.xms_marca.table);//DONE

                    response = XMSApi.getApiInterface(getApplicationContext()).getUnidades().execute();
                    dataBaseHelper.sincro(response, TablesHelper.xms_unidad_medida.table);//DONE

                    response = XMSApi.getApiInterface(getApplicationContext()).getPrecioSugerido().execute();
                    dataBaseHelper.sincro(response, TablesHelper.xms_precio_sugerido.table);//DONE

                    response = XMSApi.getApiInterface(getApplicationContext()).getPrecioBodega().execute();
                    dataBaseHelper.sincro(response, TablesHelper.xms_precio_bodega.table);//DONE

                    response = XMSApi.getApiInterface(getApplicationContext()).getClientes().execute();
                    dataBaseHelper.sincro(response, TablesHelper.xms_cliente.table);//DONE

                    response = XMSApi.getApiInterface(getApplicationContext()).getPreciosDistribuidor().execute();
                    dataBaseHelper.sincro(response, TablesHelper.xms_politica_precio_producto.table);//

                    response = XMSApi.getApiInterface(getApplicationContext()).getMarcasDistribuidor().execute();
                    dataBaseHelper.sincro(response, TablesHelper.xms_bodega_marca.table);//

                    response = XMSApi.getApiInterface(getApplicationContext()).getPoliticasPrecio().execute();
                    dataBaseHelper.sincro(response, TablesHelper.xms_politica_precio.table);//

                    response = XMSApi.getApiInterface(getApplicationContext()).getPoliticasBodega().execute();
                    dataBaseHelper.sincro(response, TablesHelper.xms_politica_precio_bodega.table);//

                    response = XMSApi.getApiInterface(getApplicationContext()).getDistribuidores().execute();
                    dataBaseHelper.sincro(response, TablesHelper.xms_empresa.table);

                    app.setSettings_validarStock(true);
                    app.setSettings_stockEnLinea(true);
                    app.setSettings_productoSinPrecio(true); */

                    app.setPref_lastSyncro(System.currentTimeMillis());
                    app.setPref_serieUsuario("001");
                    app.setPref_idPuntoVenta(1);

                    return Constants.CORRECT;
                } else {
                    return Constants.FAIL_CONNECTION;
                }
            }catch (UnauthorizedException e){
                e.printStackTrace();
                message = e.getMessage();
                return Constants.FAIL_UNAUTHORIZED;
            }catch (Resources.NotFoundException e){
                e.printStackTrace();
                return Constants.FAIL_RESOURCE;
            }catch (SocketTimeoutException e) {
                e.printStackTrace();
                return Constants.FAIL_TIMEOUT;
            }catch (NoSuchMethodException e){
                return getString(R.string.error_metodo_no_encontrado) + " " + e.getMessage();
            }catch (IOException e){
                e.printStackTrace();
                return e.getMessage();
            }catch (Exception e){
                e.printStackTrace();
                return e.getMessage();
            }
        }

        protected void onPostExecute(String result) {
            Log.d( TAG, "onPostExecute "+ result);
            switch (result){
                case Constants.CORRECT:
                    goToMenuPrincipal();
                    break;
                case Constants.FAIL_CONNECTION:
                    goToMenuPrincipal();
                    break;
                case Constants.FAIL_TIMEOUT:
                    goToMenuPrincipal();
                    break;
                case Constants.FAIL_RESOURCE:
                    retry(getString(R.string.error_resource));
                    break;
                case Constants.FAIL_UNAUTHORIZED:
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                    app.reLogin(SplashActivity.this);
                    break;
                default:
                    retry(result);
                    break;
            }
        }
    }

    private void goToMenuPrincipal() {
        Intent intent = new Intent(this, MenuPrincipalActivity.class);
        startActivity(intent);
        finish();
    }

    private void goToLogin(){
        app.logOut(this);
    }

    private void retry(String message){
        MyDetailDialog myDetailDialog = new MyDetailDialog(SplashActivity.this,R.drawable.ic_dialog_alert, getString(R.string.oops),getString(R.string.error_sincronizacion), message);
        myDetailDialog.setPositiveButton(getString(R.string.reintentar), SplashActivity.this::init);
        myDetailDialog.setNegativeButton(getString(R.string.salir), SplashActivity.this::goToLogin);
        myDetailDialog.show();
    }
}