package com.imax.app.ui.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.imax.app.App;
import com.imax.app.R;
import com.imax.app.data.api.XMSApi;
import com.imax.app.data.api.request.InspeccionRequest;
import com.imax.app.data.dao.DAOExtras;
import com.imax.app.data.dao.DAOPedido;
import com.imax.app.data.dao.DAOProducto;
import com.imax.app.intents.AntesInspeccion;
import com.imax.app.intents.CaracteristicasGenerales;
import com.imax.app.managers.DataBaseHelper;
import com.imax.app.managers.TablesHelper;
import com.imax.app.models.AsignacionModel;
import com.imax.app.models.CatalogModel;
import com.imax.app.models.ListaPrecioModel;
import com.imax.app.models.OrderDetail;
import com.imax.app.models.XMSProductModel;
import com.imax.app.ui.adapters.AutoCompleteProductoAdapter;
import com.imax.app.ui.pedido.AgregarProductoActivity;
import com.imax.app.ui.pedido.AgregarProductoArgument;
import com.imax.app.ui.pedido.PedidoActivity;
import com.imax.app.utils.Constants;
import com.imax.app.utils.MyDetailDialog;
import com.imax.app.utils.UnauthorizedException;
import com.imax.app.utils.Util;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Response;

public class RegistrarCaractGeneralesActivity extends AppCompatActivity {
    private final String TAG = getClass().getName();

    public static final int ACCION_NUEVO_REGISTRO = 1;

    public static final String EXTRA_DIRECCION = "direccion";

    public int ACCION_PEDIDO = ACCION_NUEVO_REGISTRO;

    private final int REQUEST_CODE_AGREGAR_PRODUCTO = 0;

    public String idTipoDocumentoOriginal = "";

    private boolean cabeceraGuardada = false;

    private ProgressDialog progressDialog;
    private ViewPager mViewPager;
    private String numeroDocumentoValidado;

    private DAOExtras daoExtras;


    private TextView tvGridInmueble, tvContactoSitu, tvInmuebleMultiple, tvRecibeInmueble, tvDireccionInSitu,
            tvDireccion, tvReferencia, tvDistrito, tvProvincia, tvDepartamento, tvNPisos;
    private Spinner spnTipoInmueble, spnRecibeInmueble;
    private EditText edOtro, edtComentarios, edtOtros, edtReferencia,
            edtNPisos, edtDeposito, edtEstacionamiento, edtDepto, edtDistribucion;
    private CheckBox cbVivienda, cbComercio, cbIndustria, cbEducativo, cbOther;
    private GridLayout gridCheckbox;

    private App app;
    private android.app.AlertDialog.Builder dialogBuilder;
    private android.app.AlertDialog dialog;

    private Drawable defaultBackground;
    private ProgressBar progress;
    private AntesInspeccion inspeccion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caracteristicas_general);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Util.actualizarToolBar(getString(R.string.caracteristicas), true, this, R.drawable.ic_action_back);
        progress = findViewById(R.id.progress);

        inspeccion = (AntesInspeccion) getIntent().getSerializableExtra("inspeccion");

        app = (App) getApplicationContext();
        daoExtras = new DAOExtras(getApplicationContext());

        spnTipoInmueble = findViewById(R.id.spn_modalidad);

        gridCheckbox = findViewById(R.id.grid_checkbox);
        tvGridInmueble = findViewById(R.id.tv_grid_inmueble);

        cbVivienda = findViewById(R.id.cb_vivienda);
        cbComercio = findViewById(R.id.cb_comercio);
        cbIndustria = findViewById(R.id.cb_industria);
        cbEducativo = findViewById(R.id.cb_educativo);
        cbOther = findViewById(R.id.cb_other);

        edtComentarios = findViewById(R.id.edt_comentarios);
        spnRecibeInmueble = findViewById(R.id.spn_recibe_inmueble);

        edtReferencia = findViewById(R.id.edt_referencia);
        edtOtros = findViewById(R.id.edt_otros);
        edtNPisos = findViewById(R.id.edt_n_pisos);
        edtDeposito = findViewById(R.id.edt_deposito);
        edtEstacionamiento = findViewById(R.id.edt_d);
        edtDepto = findViewById(R.id.edt_depto);
        edtDistribucion = findViewById(R.id.edt_n_depart);

        loadDataTipoInmueble();

        loadDataIfExists(inspeccion.getNumInspeccion());

        progressDialog = new ProgressDialog(RegistrarCaractGeneralesActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);

        defaultBackground = ContextCompat.getDrawable(this, R.drawable.default_border);
    }
    private void loadDataTipoInmueble(){
        List<CatalogModel> modalidades = new ArrayList<>();
        for (AsignacionModel model : daoExtras.getListAsignacion()) {
            if(model.getNumber().equalsIgnoreCase(inspeccion.getNumInspeccion())){
                modalidades.add(new CatalogModel(model.getMotiveId(), model.getMotiveName()));
            }
        }
        ArrayAdapter<CatalogModel> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, modalidades);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnTipoInmueble.setAdapter(adapter);
    }
    private void loadDataRecibeInmueble(String valorBD){
        List<CatalogModel> modalidades = new ArrayList<>();
        modalidades.add(new CatalogModel("00", "Seleccione una opción"));
        modalidades.add(new CatalogModel("01", "DESOCUPADO"));
        modalidades.add(new CatalogModel("02", "PROPIETARIO"));
        modalidades.add(new CatalogModel("03", "INQUILINO"));
        modalidades.add(new CatalogModel("04", "TERCERO"));
        modalidades.add(new CatalogModel("05", "SE DESCONOCE"));

        ArrayAdapter<CatalogModel> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, modalidades);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnRecibeInmueble.setAdapter(adapter);

        if (valorBD != null && !valorBD.trim().isEmpty()) {
            int position = getIndex(modalidades, valorBD);
            spnRecibeInmueble.setSelection(Math.max(position, 0));
        } else {
            spnRecibeInmueble.setSelection(0);
        }
    }

    private int getIndex(List<CatalogModel> modalidades, String valorBD) {
        for (int i = 0; i < modalidades.size(); i++) {
            if (modalidades.get(i).getDescripcion().equals(valorBD)) {
                return i;
            }
        }
        return -1;
    }

    private void loadDataIfExists(String numero){
        InspeccionRequest inspeccionRequest =  daoExtras.getListAsignacionByNumero(numero);

        edtOtros.setText(inspeccionRequest.getOtros());
        if (inspeccionRequest.getUsosInmueble() != null && !inspeccionRequest.getUsosInmueble().trim().isEmpty()) {
            List<String> usosSeleccionados = Arrays.asList(inspeccionRequest.getUsosInmueble().split(", "));
            cbVivienda.setChecked(usosSeleccionados.contains("Vivienda"));
            cbComercio.setChecked(usosSeleccionados.contains("Comercio"));
            cbIndustria.setChecked(usosSeleccionados.contains("Industria"));
            cbEducativo.setChecked(usosSeleccionados.contains("Educativo"));
            cbOther.setChecked(usosSeleccionados.contains("Otro"));
        }
        edtComentarios.setText(inspeccionRequest.getComentarios());
        edtNPisos.setText(inspeccionRequest.getnPisos());
        edtDistribucion.setText(inspeccionRequest.getDistribucion());
        edtReferencia.setText(inspeccionRequest.getReferencia());
        edtDeposito.setText(inspeccionRequest.getDeposito());
        edtEstacionamiento.setText(inspeccionRequest.getEstacionamiento());
        edtDepto.setText(inspeccionRequest.getDepto());
        loadDataRecibeInmueble(inspeccionRequest.getRecibeInmueble());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_pedido, menu);
        menu.findItem(R.id.menu_pedido_siguiente).setVisible(true);
        menu.findItem(R.id.menu_pedido_guardar).setVisible(false);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_pedido_siguiente:
                if (validarCampos()) {
                    CaracteristicasGenerales caracteristicasGenerales = new CaracteristicasGenerales(
                            spnTipoInmueble.getSelectedItem().toString(),
                            edtOtros.getText().toString(),
                            cbVivienda.isChecked(),
                            cbComercio.isChecked(),
                            cbIndustria.isChecked(),
                            cbEducativo.isChecked(),
                            cbOther.isChecked(),
                            edtComentarios.getText().toString(),
                            spnRecibeInmueble.getSelectedItem().toString(),
                            edtNPisos.getText().toString(),
                            edtDistribucion.getText().toString(),
                            edtReferencia.getText().toString(),
                            edtDeposito.getText().toString(),
                            edtEstacionamiento.getText().toString(),
                            edtDepto.getText().toString()
                    );

                    daoExtras.actualizarRegistroCaracGeneral(caracteristicasGenerales, inspeccion.getNumInspeccion());

                    Intent intent = new Intent(RegistrarCaractGeneralesActivity.this, RegistroCaracteristicasEdificacionActivity.class);

                    intent.putExtra("inspeccion", inspeccion);
                    intent.putExtra("caracteristicasGenerales", caracteristicasGenerales);

                    startActivity(intent);
                }
                break;
            case R.id.menu_pedido_guardar:

                break;
            case android.R.id.home:
                System.out.println("VOLVER 1 SECCION");
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean validarCampos() {
        boolean isValid = true;
        Drawable errorBackground = ContextCompat.getDrawable(this, R.drawable.error_border);

        if (!cbVivienda.isChecked() && !cbComercio.isChecked() && !cbIndustria.isChecked()
                && !cbEducativo.isChecked() && !cbOther.isChecked()) {
            tvGridInmueble.setBackground(errorBackground);
            isValid = false;
        } else {
            tvGridInmueble.setBackground(defaultBackground);
        }

        isValid &= validarSpinner(spnTipoInmueble, errorBackground);
        isValid &= validarSpinner(spnRecibeInmueble, errorBackground);
        isValid &= validarEditText(edtDistribucion, errorBackground);
        isValid &= validarEditText(edtNPisos, errorBackground);

        if (!isValid) {
            Toast.makeText(this, "Por favor, complete todos los campos obligatorios", Toast.LENGTH_SHORT).show();
        }

        return isValid;
    }

    private boolean validarEditText(EditText editText, Drawable errorBackground) {
        if (editText.getText().toString().isEmpty()) {
            editText.setBackground(errorBackground);
            return false;
        } else {
            editText.setBackground(defaultBackground);
            return true;
        }
    }

    private boolean validarSpinner(Spinner spinner, Drawable errorBackground) {
        String defaultValue = "Seleccione una opción";
        if (spinner.getSelectedItem().toString().equals(defaultValue)) {
            spinner.setBackground(errorBackground);
            return false;
        } else {
            spinner.setBackground(defaultBackground);
            return true;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void DialogoConfirmacion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RegistrarCaractGeneralesActivity.this);
        if (ACCION_PEDIDO == ACCION_NUEVO_REGISTRO) {
            builder.setTitle(getString(R.string.descartar_pedido));
            builder.setMessage(getString(R.string.se_perdera_pedido));
        } else {
            builder.setTitle(getString(R.string.descartar_cambios));
            builder.setMessage(getString(R.string.se_perdera_cambios));
        }
        builder.setNegativeButton(getString(R.string.cancelar), null);
        builder.show();

    }

    public void noPermitirCerrar() {
        Util.actualizarToolBar("", false, this);
        cabeceraGuardada = true; //Para que el onBack valide y no deje retroceder
    }

    void showAlertError(String titulo, String mensaje) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.ic_dialog_error);
        builder.setTitle(titulo);
        builder.setMessage(mensaje);
        builder.setCancelable(false);
        builder.setNegativeButton(getString(R.string.aceptar), (dialog, which) -> finish());
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_AGREGAR_PRODUCTO) {
            if (resultCode == RESULT_OK) {
                AgregarProductoArgument argument = (AgregarProductoArgument) data.getSerializableExtra(
                        AgregarProductoActivity.EXTRA_PRODUCTO_AGREGADO);

            }
        }
    }


    public void showLoader() {
        progressDialog.show();
    }

    public void hideLoader() {
        progressDialog.dismiss();
    }

    public void showDialogoPostEnvio(@StringRes int tituloRes, @StringRes int mensajeRes, @DrawableRes int icon) {
        AlertDialog.Builder builder = new AlertDialog.Builder(RegistrarCaractGeneralesActivity.this);
        builder.setTitle(tituloRes);
        builder.setMessage(mensajeRes);
        builder.setIcon(icon);
        builder.setCancelable(false);

        builder.setPositiveButton(getString(R.string.aceptar), (dialog, which) -> {
            /*Intent intent = new Intent(getApplicationContext(), DetallePedidoActivity.class);
            intent.putExtra("numeroPedido",numeroPedido);
            intent.putExtra("idCliente",idCliente);
            intent.putExtra("nombreCliente",autocomplete_busqueda.getText().toString());
            startActivity(intent); */
            setResult(RESULT_OK);
            finish();
        });
        builder.show();
    }

}
