package com.imax.app.ui.activity;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
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
import com.imax.app.intents.AntesInspeccion;
import com.imax.app.managers.DataBaseHelper;
import com.imax.app.managers.TablesHelper;
import com.imax.app.models.AsignacionModel;
import com.imax.app.models.CatalogModel;
import com.imax.app.ui.pedido.AgregarProductoActivity;
import com.imax.app.ui.pedido.AgregarProductoArgument;
import com.imax.app.utils.Constants;
import com.imax.app.utils.MyDetailDialog;
import com.imax.app.utils.UnauthorizedException;
import com.imax.app.utils.Util;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Response;

public class RegistroInspeccionActivity extends AppCompatActivity {
    private final String TAG = getClass().getName();

    public static final int ACCION_NUEVO_REGISTRO = 1;

    public static final String EXTRA_DIRECCION = "direccion";


    private final int REQUEST_CODE_AGREGAR_PRODUCTO = 0;

    public String idTipoDocumentoOriginal = "";

    private boolean cabeceraGuardada = false;

    private ProgressDialog progressDialog;
    private ViewPager mViewPager;
    private String numeroDocumentoValidado;

    private DAOExtras daoExtras;

    private EditText edtFecha, edtHora, edtContacto, edtDireccion, edtDistrito, edtProvincia, edtDepartamento;
    private Spinner spnAsignarNumero;
    private Spinner spinnerInspeccion;
    private TextView txtLatitude, txtLongitude;

    private Drawable defaultBackground;
    private Calendar calendar;
    private App app;
    private android.app.AlertDialog.Builder dialogBuilder;
    private android.app.AlertDialog dialog;

    Button btnOpenMaps;
    private ProgressBar progress;
    private DataBaseHelper dataBaseHelper;
    private ArrayList<AsignacionModel> lista = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_antes_inspeccion);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Util.actualizarToolBar(getString(R.string.nuevo_registro), true, this, R.drawable.ic_action_close);
        progress = findViewById(R.id.progress);
        dataBaseHelper = DataBaseHelper.getInstance(getApplicationContext());

        app = (App) getApplicationContext();
        calendar = Calendar.getInstance();
        daoExtras = new DAOExtras(getApplicationContext());

        edtFecha = findViewById(R.id.edt_fecha);
        edtHora = findViewById(R.id.edt_hora);
        edtContacto = findViewById(R.id.ed_contacto_situ);
        spnAsignarNumero = findViewById(R.id.spn_asignarNumero);
        spinnerInspeccion = findViewById(R.id.spn_inspeccion);

        edtDireccion = findViewById(R.id.edt_direccion);
        edtDistrito = findViewById(R.id.edt_distrito);
        edtProvincia = findViewById(R.id.edt_provincia);
        edtDepartamento = findViewById(R.id.edt_departamento);

        txtLatitude = findViewById(R.id.txt_latitude);
        txtLongitude = findViewById(R.id.txt_longitude);

        btnOpenMaps = findViewById(R.id.btn_open_maps);

        edtFecha.setOnClickListener(v -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(RegistroInspeccionActivity.this,
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        edtFecha.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1);
                    }, year, month, day);
            datePickerDialog.show();
        });
        edtHora.setOnClickListener(v -> {
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(RegistroInspeccionActivity.this,
                    (TimePicker view, int hourOfDay, int minute1) -> {
                        edtHora.setText(String.format("%02d:%02d", hourOfDay, minute1));
                    }, hour, minute, true);
            timePickerDialog.show();
        });

        new async_sincronizacion().execute();

        btnOpenMaps.setOnClickListener(v -> {
            String latitude = txtLatitude.getText().toString();
            String longitude = txtLongitude.getText().toString();

            if (!latitude.equals("-") && !longitude.equals("-")) {
                openGoogleMaps(latitude, longitude);
            } else {
                Toast.makeText(RegistroInspeccionActivity.this, "Latitud y Longitud no válidas", Toast.LENGTH_SHORT).show();
            }
        });

        loadDataModalidad();

        spnAsignarNumero.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //edtContacto.setText(lista.get(position).getInspectorName());

                edtDireccion.setText(lista.get(position).getAddress());
                edtDistrito.setText(lista.get(position).getCustomField1());
                edtProvincia.setText(lista.get(position).getCustomField2());
                edtDepartamento.setText(lista.get(position).getCustomField3());

                txtLatitude.setText(lista.get(position).getPartnerLatitude());
                txtLongitude.setText(lista.get(position).getPartnerLongitude());

                CatalogModel modalidadSeleccionada =
                        new CatalogModel(lista.get(position).getTypeId(),
                                lista.get(position).getTypeName());
                loadDataTipoInscripcion(modalidadSeleccionada);

                setDateTimeInEditText(lista.get(position).getInspectionDate(), edtFecha, edtHora);

                loadDataIfExists(lista.get(position).getNumber());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        progressDialog = new ProgressDialog(RegistroInspeccionActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);

        configurarTextWatcher(edtContacto);
    }

    private void configurarTextWatcher(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Verifica si el campo no está vacío
                if (!s.toString().trim().isEmpty()) {
                    // Cambia el fondo a su estado por defecto
                    editText.setBackgroundResource(android.R.drawable.edit_text); // Usando el drawable de sistema directamente
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Si el campo está vacío, aplica el borde de error
                if (s.toString().trim().isEmpty()) {
                    editText.setBackground(ContextCompat.getDrawable(RegistroInspeccionActivity.this, R.drawable.error_border)); // Aplica el borde de error
                }
            }
        });
    }



    private void loadDataIfExists(String numero){
        InspeccionRequest inspeccionRequest =  daoExtras.getListAsignacionByNumero(numero);

        if(inspeccionRequest.getNumInspeccion().trim().length() != 0){
            edtContacto.setText(inspeccionRequest.getContacto());
        }else{
            edtContacto.setText("");
        }
    }

    public void setDateTimeInEditText(String fecha, EditText edtFecha, EditText edtHora) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;

        try {
            date = sdf.parse(fecha);  // Convierte el string a Date
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;  // Mes de 0 a 11, por eso sumamos 1
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        String formattedDate = String.format("%02d/%02d/%04d", day, month, year);  // dd/MM/yyyy
        String formattedTime = String.format("%02d:%02d", hour, minute);  // HH:mm

        edtFecha.setText(formattedDate);
        edtHora.setText(formattedTime);
    }
    private void openGoogleMaps(String latitude, String longitude) {
        try {
            double lat = Double.parseDouble(latitude);
            double lon = Double.parseDouble(longitude);
            String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f", lat, lon, lat, lon);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setPackage("com.google.android.apps.maps");

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "Google Maps no está instalado", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Coordenadas inválidas", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadDataModalidad(){
        List<CatalogModel> modalidades = new ArrayList<>();
        modalidades.add(new CatalogModel("00", "Seleccione una opción"));
        modalidades.add(new CatalogModel("01", "PRESENCIAL"));
        modalidades.add(new CatalogModel("02", "VIRTUAL"));
    }
    private void loadDataTipoInscripcion(CatalogModel modalidadSeleccionada) {
        List<CatalogModel> inscripciones = new ArrayList<>();

        if(modalidadSeleccionada.getDescripcion().trim().length() != 0){
            inscripciones.add(modalidadSeleccionada);
        }else{
            inscripciones.add(new CatalogModel("00", "Seleccione una opción"));
            inscripciones.add(new CatalogModel("01", "INTERIOR INSCRIPCIÓN"));
            inscripciones.add(new CatalogModel("02", "EXTERIOR INSCRIPCIÓN"));
        }

        ArrayAdapter<CatalogModel> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, inscripciones);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerInspeccion.setAdapter(adapter);

        if (modalidadSeleccionada == null || modalidadSeleccionada.getCodigo().isEmpty()) {
            spinnerInspeccion.setSelection(0); // Selecciona la opción "Seleccione una opción"
        } else {
            for (int i = 0; i < inscripciones.size(); i++) {
                if (inscripciones.get(i).getCodigo().equals(modalidadSeleccionada.getCodigo())) {
                    spinnerInspeccion.setSelection(i); // Preselecciona el valor correspondiente
                    break;
                }
            }
        }
    }

    private boolean validarCampos() {
        boolean isValid = true;
        Drawable errorBackground = ContextCompat.getDrawable(this, R.drawable.error_border);

        isValid &= validarEditText(edtContacto, errorBackground);
        isValid &= validarEditText(edtHora, errorBackground);
        isValid &= validarEditText(edtFecha, errorBackground);

        //isValid &= validarSpinner(spnAsignarNumero, errorBackground);
        //isValid &= validarSpinner(spinnerModalidad, errorBackground);
        isValid &= validarSpinner(spinnerInspeccion, errorBackground);

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
                    AntesInspeccion inspeccion = new AntesInspeccion(
                            spnAsignarNumero.getSelectedItem().toString(),
                            "",
                            spinnerInspeccion.getSelectedItem().toString(),
                            edtFecha.getText().toString(),
                            edtHora.getText().toString(),
                            edtContacto.getText().toString(),
                            txtLatitude.getText().toString(),
                            txtLongitude.getText().toString(),
                            edtDireccion.getText().toString(),
                            edtDistrito.getText().toString(),
                            edtProvincia.getText().toString(),
                            edtDepartamento.getText().toString()
                    );

                    if(daoExtras.existeRegistro(spnAsignarNumero.getSelectedItem().toString())){
                        daoExtras.actualizarRegistroInpeccion(inspeccion);
                    }else{
                        daoExtras.crearRegistro(inspeccion);
                    }

                    Intent intent = new Intent(RegistroInspeccionActivity.this, RegistrarCaractGeneralesActivity.class);
                    intent.putExtra("inspeccion", inspeccion);
                    startActivity(intent);
                }
                break;
            case R.id.menu_pedido_guardar:
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void DialogoConfirmacion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RegistroInspeccionActivity.this);
        builder.setTitle(getString(R.string.descartar_cambios));
        builder.setMessage(getString(R.string.se_perdera_cambios));
        builder.setNegativeButton(getString(R.string.cancelar), null);
        builder.show();

    }

    public void noPermitirCerrar() {
        Util.actualizarToolBar("", false, this);
        cabeceraGuardada = true;
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

    class async_sincronizacion extends AsyncTask<Void, String, String> {

        protected void onPreExecute() {
            progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {

                if (Util.isConnectingToRed(RegistroInspeccionActivity.this)) {
                    Log.d(TAG, "sincronizando datos...");
                    Response<ResponseBody> response;

                    String domain = "[[\"user_id.login\",\"=\",\"jose.lunarejo@imax.com.pe\"],[\"stage_id.name\",\"in\",[\"Inspección (Perito)\",\"Elaboración (Perito)\"]]]";

                    response = XMSApi.getApiEasyfact(RegistroInspeccionActivity.this.getApplicationContext())
                            .obtenerTickets(domain, 500).execute();
                    dataBaseHelper.sincro(response, TablesHelper.xms_asignacion.table);

                    return Constants.CORRECT;
                } else {
                    return Constants.FAIL_CONNECTION;
                }
            } catch (UnauthorizedException e) {
                e.printStackTrace();
                return Constants.FAIL_UNAUTHORIZED;
            } catch (Resources.NotFoundException e) {
                e.printStackTrace();
                return Constants.FAIL_RESOURCE;
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                return Constants.FAIL_TIMEOUT;
            } catch (IOException e) {
                e.printStackTrace();
                return e.getMessage();
            } catch (Exception e) {
                e.printStackTrace();
                return e.getMessage();
            }
        }

        protected void onPostExecute(String result) {
            Log.d(TAG, "onPostExecute " + result);
            progress.setVisibility(View.GONE);
            switch (result) {
                case Constants.CORRECT:
                case Constants.FAIL_UNAUTHORIZED:
                case Constants.FAIL_TIMEOUT:

                    break;
                case Constants.FAIL_CONNECTION:
                    Toast.makeText(RegistroInspeccionActivity.this, R.string.error_no_autorizado, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    MyDetailDialog myDetailDialog = new MyDetailDialog(RegistroInspeccionActivity.this,
                            R.drawable.ic_dialog_alert, getString(R.string.oops), getString(R.string.error_sincronizacion), result);
                    myDetailDialog.show();
                    break;
            }
            refreshLista();
        }
    }

    public void refreshLista() {
        lista.clear();
        lista.addAll(daoExtras.getListAsignacion());

        ArrayList<String> array = new ArrayList<String>();
        for (AsignacionModel model : lista) {
            array.add(model.getNumber());
        }
        ArrayAdapter adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.my_spinner_item, array);
        spnAsignarNumero.setAdapter(adapter);

    }

    public void showLoader() {
        progressDialog.show();
    }
    public void hideLoader() {
        progressDialog.dismiss();
    }

    public void showDialogoPostEnvio(@StringRes int tituloRes, @StringRes int mensajeRes, @DrawableRes int icon) {
        AlertDialog.Builder builder = new AlertDialog.Builder(RegistroInspeccionActivity.this);
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
