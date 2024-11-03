package com.imax.app.ui.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.imax.app.App;
import com.imax.app.R;
import com.imax.app.data.api.request.InspeccionRequest;
import com.imax.app.data.dao.DAOExtras;
import com.imax.app.intents.AntesInspeccion;
import com.imax.app.intents.CaracteristicasEdificacion;
import com.imax.app.intents.CaracteristicasGenerales;
import com.imax.app.models.CatalogModel;
import com.imax.app.ui.pedido.AgregarProductoActivity;
import com.imax.app.ui.pedido.AgregarProductoArgument;
import com.imax.app.utils.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class RegistroCaracteristicasEdificacionActivity extends AppCompatActivity {
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

    private TextView tv_radioGroup;
    private EditText edtOtroEstructura, edtOtroAlbañeria, edtOtroPisos, edtOtroPuertas,
            edtOtroVentana, edtOtroMampara, edtOtroCocina, edtOtroBaño, edtOtroSanitarias, edtOtroElectricas;
    private Spinner spnTipoPuerta, spnMaterialPuerta, spnSistemaPuerta,
            spnMarcoVentana, spnVidrioVentana, spnSistemaVentana,
            spnMarcoMampara, spnVidrioMampara, spnSistemaMampara,
            spnMuebleCocina, spnMuebleCocina2, spnTablero,
            spnLavaderos, spnSanitarioTipo, spnSanitarioColor,
            spnSanitario, spnIss, spnIiee;
    private Button btnSiguiente;
    RadioGroup radioGroupSistemaIncendio;
    RadioButton radioTiene, radioNoTiene;
    private MultiAutoCompleteTextView multiAutoCompleteTextView, multiCompleteMuros, multiCompleteRevestimiento,
            multiCompletePisos, multiCompletePisosCocina, multiCompleteParedesCocina,multiCompletePisosBaños,
            multiCompleteParedesBaño;
    private App app;
    private android.app.AlertDialog.Builder dialogBuilder;
    private android.app.AlertDialog dialog;
    private AntesInspeccion inspeccion;
    private CaracteristicasGenerales caracteristicasGenerales;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caracteristicas_edificacion);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Util.actualizarToolBar(getString(R.string.nuevo_registro), true, this, R.drawable.ic_action_back);

        inspeccion = (AntesInspeccion) getIntent().getSerializableExtra("inspeccion");
        caracteristicasGenerales = (CaracteristicasGenerales) getIntent().getSerializableExtra("caracteristicasGenerales");

        app = (App) getApplicationContext();

        daoExtras = new DAOExtras(getApplicationContext());

        edtOtroEstructura = findViewById(R.id.edt_otro_estructura);
        edtOtroAlbañeria = findViewById(R.id.edt_otros_albañeria);
        edtOtroPisos = findViewById(R.id.edt_otros_pisos);
        edtOtroPuertas = findViewById(R.id.edt_otros_puertas);
        edtOtroVentana = findViewById(R.id.edt_otros_ventana);
        edtOtroMampara = findViewById(R.id.edt_otros_mampara);
        edtOtroCocina = findViewById(R.id.edt_otros_cocina);
        edtOtroBaño = findViewById(R.id.edt_otros_baño);
        edtOtroSanitarias = findViewById(R.id.edt_otros_sanitarias);
        edtOtroElectricas = findViewById(R.id.edt_otros_electricas);
        spnTipoPuerta = findViewById(R.id.spn_tipo_puertas);
        spnMaterialPuerta = findViewById(R.id.spn_material_puertas);
        spnSistemaPuerta = findViewById(R.id.spn_sistemas_puertas);
        spnMarcoVentana = findViewById(R.id.spn_marco_ventana);
        spnVidrioVentana = findViewById(R.id.spn_vidrio_ventana);
        spnSistemaVentana = findViewById(R.id.spn_sistemas_ventana);
        spnMarcoMampara = findViewById(R.id.spn_marco_mamparas);
        spnVidrioMampara = findViewById(R.id.spn_vidrio_mamparas);
        spnSistemaMampara = findViewById(R.id.spn_sistemas_mamparas);
        spnMuebleCocina = findViewById(R.id.spn_mueble_cocina);
        spnMuebleCocina2 = findViewById(R.id.spn_mueble_cocina2);
        spnTablero = findViewById(R.id.spn_tableros);
        spnLavaderos = findViewById(R.id.spn_lavaderos);
        spnSanitarioTipo = findViewById(R.id.spn_mueble_baño);
        spnSanitarioColor = findViewById(R.id.spn_sanitario_color);
        spnSanitario = findViewById(R.id.spn_sanitarios);
        spnIss = findViewById(R.id.spn_iss);
        spnIiee = findViewById(R.id.spn_iiee);
        tv_radioGroup = findViewById(R.id.radioGroup);
        radioGroupSistemaIncendio = findViewById(R.id.radioGroupSistemaIncendio);
        radioTiene = findViewById(R.id.radio_tiene);
        radioNoTiene = findViewById(R.id.radio_no_tiene);
        multiAutoCompleteTextView = findViewById(R.id.multiCompleteEstructura);
        multiCompleteMuros = findViewById(R.id.multiCompleteMuros);
        multiCompleteRevestimiento = findViewById(R.id.multiCompleteRevestimiento);
        multiCompletePisos = findViewById(R.id.multiCompletePisos);
        multiCompletePisosCocina = findViewById(R.id.multiCompletePisosCocina);
        multiCompleteParedesCocina = findViewById(R.id.multiCompleteParedesCocina);
        multiCompletePisosBaños = findViewById(R.id.multiCompletePisosBaños);
        multiCompleteParedesBaño = findViewById(R.id.multiCompleteParedesBaños);

        loadDataIfExists(inspeccion.getNumInspeccion());

        progressDialog = new ProgressDialog(RegistroCaracteristicasEdificacionActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);


        multiAutoCompleteTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // El usuario ha seleccionado la vista: quita el borde rojo
                    multiAutoCompleteTextView.setBackground(ContextCompat.getDrawable(v.getContext(), android.R.drawable.edit_text));
                    // Forzar la apertura de las opciones
                    multiAutoCompleteTextView.showDropDown();
                }
            }
        });

        multiCompleteMuros.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // El usuario ha seleccionado la vista: quita el borde rojo
                    multiCompleteMuros.setBackground(ContextCompat.getDrawable(v.getContext(), android.R.drawable.edit_text));

                    // Forzar la apertura de las opciones
                    multiCompleteMuros.showDropDown();
                }
            }
        });

        multiCompleteRevestimiento.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // El usuario ha seleccionado la vista: quita el borde rojo
                    multiCompleteRevestimiento.setBackground(ContextCompat.getDrawable(v.getContext(), android.R.drawable.edit_text));

                    // Forzar la apertura de las opciones
                    multiCompleteRevestimiento.showDropDown();
                }
            }
        });

        multiCompletePisos.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View v, boolean hasFocus){
                if(hasFocus){
                    multiCompletePisos.setBackground(ContextCompat.getDrawable(v.getContext(), android.R.drawable.edit_text));
                }
                multiCompletePisos.showDropDown();
            }
        });

        multiCompletePisosCocina.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View v, boolean hasFocus){
                if(hasFocus){
                    multiCompletePisosCocina.setBackground(ContextCompat.getDrawable(v.getContext(), android.R.drawable.edit_text));
                }
                multiCompletePisosCocina.showDropDown();
            }
        });

        multiCompleteParedesCocina.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View v, boolean hasFocus){
                if(hasFocus){
                    multiCompleteParedesCocina.setBackground(ContextCompat.getDrawable(v.getContext(), android.R.drawable.edit_text));
                }
                multiCompleteParedesCocina.showDropDown();
            }
        });

        multiCompletePisosBaños.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View v, boolean hasFocus){
                if(hasFocus){
                    multiCompletePisosBaños.setBackground(ContextCompat.getDrawable(v.getContext(), android.R.drawable.edit_text));
                }
                multiCompletePisosBaños.showDropDown();
            }
        });

        multiCompleteParedesBaño.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View v, boolean hasFocus){
                if(hasFocus){
                    multiCompleteParedesBaño.setBackground(ContextCompat.getDrawable(v.getContext(), android.R.drawable.edit_text));
                }
                multiCompleteParedesBaño.showDropDown();
            }
        });

        spnTipoPuerta.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // El usuario ha seleccionado una opción: quita el borde rojo
                spnTipoPuerta.setBackground(ContextCompat.getDrawable(parent.getContext(), android.R.drawable.edit_text));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Opcional: define qué hacer si no se selecciona nada, si es necesario
            }
        });

        spnMaterialPuerta.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // El usuario ha seleccionado una opción: quita el borde rojo
                spnMaterialPuerta.setBackground(ContextCompat.getDrawable(parent.getContext(), android.R.drawable.edit_text));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Opcional: define qué hacer si no se selecciona nada, si es necesario
            }
        });

        spnSistemaPuerta.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // El usuario ha seleccionado una opción: quita el borde rojo
                spnSistemaPuerta.setBackground(ContextCompat.getDrawable(parent.getContext(), android.R.drawable.edit_text));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Opcional: define qué hacer si no se selecciona nada, si es necesario
            }
        });

        spnMarcoVentana.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // El usuario ha seleccionado una opción: quita el borde rojo
                spnMarcoVentana.setBackground(ContextCompat.getDrawable(parent.getContext(), android.R.drawable.edit_text));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Opcional: define qué hacer si no se selecciona nada, si es necesario
            }
        });

        spnVidrioVentana.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // El usuario ha seleccionado una opción: quita el borde rojo
                spnVidrioVentana.setBackground(ContextCompat.getDrawable(parent.getContext(), android.R.drawable.edit_text));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Opcional: define qué hacer si no se selecciona nada, si es necesario
            }
        });

        spnSistemaVentana.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // El usuario ha seleccionado una opción: quita el borde rojo
                spnSistemaVentana.setBackground(ContextCompat.getDrawable(parent.getContext(), android.R.drawable.edit_text));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Opcional: define qué hacer si no se selecciona nada, si es necesario
            }
        });

        spnMuebleCocina.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // El usuario ha seleccionado una opción: quita el borde rojo
                spnMuebleCocina.setBackground(ContextCompat.getDrawable(parent.getContext(), android.R.drawable.edit_text));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Opcional: define qué hacer si no se selecciona nada, si es necesario
            }
        });

        spnMuebleCocina2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // El usuario ha seleccionado una opción: quita el borde rojo
                spnMuebleCocina2.setBackground(ContextCompat.getDrawable(parent.getContext(), android.R.drawable.edit_text));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Opcional: define qué hacer si no se selecciona nada, si es necesario
            }
        });

        spnTablero.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // El usuario ha seleccionado una opción: quita el borde rojo
                spnTablero.setBackground(ContextCompat.getDrawable(parent.getContext(), android.R.drawable.edit_text));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Opcional: define qué hacer si no se selecciona nada, si es necesario
            }
        });


        spnLavaderos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // El usuario ha seleccionado una opción: quita el borde rojo
                spnLavaderos.setBackground(ContextCompat.getDrawable(parent.getContext(), android.R.drawable.edit_text));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Opcional: define qué hacer si no se selecciona nada, si es necesario
            }
        });

        spnSanitarioTipo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // El usuario ha seleccionado una opción: quita el borde rojo
                spnSanitarioTipo.setBackground(ContextCompat.getDrawable(parent.getContext(), android.R.drawable.edit_text));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Opcional: define qué hacer si no se selecciona nada, si es necesario
            }
        });

        spnSanitarioColor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // El usuario ha seleccionado una opción: quita el borde rojo
                spnSanitarioColor.setBackground(ContextCompat.getDrawable(parent.getContext(), android.R.drawable.edit_text));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Opcional: define qué hacer si no se selecciona nada, si es necesario
            }
        });

        spnSanitario.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // El usuario ha seleccionado una opción: quita el borde rojo
                spnSanitario.setBackground(ContextCompat.getDrawable(parent.getContext(), android.R.drawable.edit_text));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Opcional: define qué hacer si no se selecciona nada, si es necesario
            }
        });

        spnIss.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // El usuario ha seleccionado una opción: quita el borde rojo
                spnIss.setBackground(ContextCompat.getDrawable(parent.getContext(), android.R.drawable.edit_text));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Opcional: define qué hacer si no se selecciona nada, si es necesario
            }
        });

        spnIiee.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // El usuario ha seleccionado una opción: quita el borde rojo
                spnIiee.setBackground(ContextCompat.getDrawable(parent.getContext(), android.R.drawable.edit_text));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Opcional: define qué hacer si no se selecciona nada, si es necesario
            }
        });

        radioGroupSistemaIncendio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // Cuando el usuario selecciona una opción, quita el borde rojo del tv_radioGroup
                tv_radioGroup.setBackground(ContextCompat.getDrawable(group.getContext(), R.drawable.default_border)); // Cambia "default_border" por tu fondo sin el borde rojo
            }
        });
    }



    private void loadDataIfExists(String numero){
        InspeccionRequest inspeccionRequest =  daoExtras.getListAsignacionByNumero(numero);

        loadDataEstructura(inspeccionRequest.getEstructura());
        loadDataMuro(inspeccionRequest.getMuros());
        loadDataRevestimiento(inspeccionRequest.getRevestimiento());
        loadDataPisos(inspeccionRequest.getPisos());
        loadDataPisosCocina(inspeccionRequest.getPisosCocina());
        loadDataParedesCocina(inspeccionRequest.getParedesCocina());
        loadDataPisosBaños(inspeccionRequest.getPisosBaños());
        loadDataParedesBaños(inspeccionRequest.getParedesBaño());

        loadDataTipoPuerta(inspeccionRequest.getTipoPuerta());
        loadDataMaterialPuerta(inspeccionRequest.getMaterialPuerta());
        loadDataSistemaPuerta(inspeccionRequest.getSistemaPuerta());
        loadDataMarcoVentana(inspeccionRequest.getMarcoVentana());
        loadDataVidrioVentana(inspeccionRequest.getVidrioVentana());
        loadDataSistemaVentana(inspeccionRequest.getSistemaVentana());
        loadDataMarcoMampara(inspeccionRequest.getMarcoMampara());
        loadDataVidrioMampara(inspeccionRequest.getVidrioMampara());
        loadDataSistemasMampara(inspeccionRequest.getSistemaMampara());
        loadDataMueblesCocina(inspeccionRequest.getMuebleCocina());
        loadDataMueblesCocina2(inspeccionRequest.getMuebleCocina2());
        loadDataTablero(inspeccionRequest.getTablero());
        loadDataLavadero(inspeccionRequest.getLavaderos());
        loadDataTipoSanitarios(inspeccionRequest.getSanitarioTipo());
        loadDataColorSanitarios(inspeccionRequest.getSanitarioColor());
        loadDataSanitarios(inspeccionRequest.getSanitario());
        loadDataISS(inspeccionRequest.getIss());
        loadDataIIEE(inspeccionRequest.getIiee());

        edtOtroEstructura.setText(inspeccionRequest.getOtroEstructura());
        edtOtroAlbañeria.setText(inspeccionRequest.getOtroAlbañeria());
        edtOtroBaño.setText(inspeccionRequest.getOtroBaño());
        edtOtroCocina.setText(inspeccionRequest.getOtroCocina());
        edtOtroElectricas.setText(inspeccionRequest.getOtroElectricas());
        edtOtroMampara.setText(inspeccionRequest.getOtroMampara());
        edtOtroPisos.setText(inspeccionRequest.getOtroPisos());
        edtOtroPuertas.setText(inspeccionRequest.getOtroPuertas());
        edtOtroSanitarias.setText(inspeccionRequest.getOtroSanitarias());
        edtOtroVentana.setText(inspeccionRequest.getOtroVentana());

        String sistemaIncendio = inspeccionRequest.getSistemaIncendio();

        if ("Tiene".equals(sistemaIncendio)) {
            radioTiene.setChecked(true);
            radioNoTiene.setChecked(false);
        } else if ("No tiene".equals(sistemaIncendio)) {
            radioNoTiene.setChecked(true);
            radioTiene.setChecked(false);
        } else {
            radioTiene.setChecked(false);
            radioNoTiene.setChecked(false);
        }

    }
    private void loadDataEstructura(String valoresBD) {
        List<CatalogModel> opciones = new ArrayList<>();
        opciones.add(new CatalogModel("001", "Laminares Curvadas"));
        opciones.add(new CatalogModel("002", "Pórticos de Concreto Armado"));
        opciones.add(new CatalogModel("003", "Albañilería Confinada"));
        opciones.add(new CatalogModel("004", "Albañilería sin Confinar"));
        opciones.add(new CatalogModel("005", "Sistema Mixto, Placas y Pórticos de C°A°"));
        opciones.add(new CatalogModel("006", "Adobe"));
        opciones.add(new CatalogModel("007", "De Madera"));
        opciones.add(new CatalogModel("008", "Vigas y columnas de concreto armado"));
        opciones.add(new CatalogModel("009", "Otro"));

        setupMultiAutoCompleteTextView(multiAutoCompleteTextView, opciones, valoresBD);
    }
    private void loadDataMuro(String valoresBD){
        List<CatalogModel> opciones = new ArrayList<>();
        opciones.add(new CatalogModel("001", "Adobe"));
        opciones.add(new CatalogModel("002", "Pircado con mezcla de barro"));
        opciones.add(new CatalogModel("003", "Drywall / Material liviano"));
        opciones.add(new CatalogModel("004", "Albañilería (Ladrillo)"));
        opciones.add(new CatalogModel("005", "Albañilería (Concreto)"));
        opciones.add(new CatalogModel("006", "Albañilería (Calcáreo)"));
        opciones.add(new CatalogModel("007", "Placas de Concreto"));
        opciones.add(new CatalogModel("008", "De Piedra"));
        opciones.add(new CatalogModel("009", "Otro"));

        setupMultiAutoCompleteTextView(multiCompleteMuros, opciones, valoresBD);
    }
    private void loadDataRevestimiento(String valoresBD){
        List<CatalogModel> opciones = new ArrayList<>();
        opciones.add(new CatalogModel("001", "Tarrajeo"));
        opciones.add(new CatalogModel("002", "Pintura"));
        opciones.add(new CatalogModel("003", "Tarrajeo y Pintura"));
        opciones.add(new CatalogModel("004", "Cerámico"));
        opciones.add(new CatalogModel("005", "Mayólica"));
        opciones.add(new CatalogModel("006", "Porcelanato"));
        opciones.add(new CatalogModel("007", "Concreto Visto"));
        opciones.add(new CatalogModel("008", "Sin tarrajeo con Pintura"));
        opciones.add(new CatalogModel("009", "Papel mural"));
        opciones.add(new CatalogModel("010", "Otro"));

        setupMultiAutoCompleteTextView(multiCompleteRevestimiento, opciones, valoresBD);
    }
    private void loadDataPisos(String valoresBD){
        List<CatalogModel> opciones = new ArrayList<>();
        opciones.add(new CatalogModel("001", "Alfombra"));
        opciones.add(new CatalogModel("002", "Bambú"));
        opciones.add(new CatalogModel("003", "Cemento Bruñado"));
        opciones.add(new CatalogModel("004", "Cemento Pulido"));
        opciones.add(new CatalogModel("005", "Cerámica"));
        opciones.add(new CatalogModel("006", "Cerámica Importada"));
        opciones.add(new CatalogModel("007", "Ladrillo Pastelero"));
        opciones.add(new CatalogModel("008", "Lajas"));
        opciones.add(new CatalogModel("009", "Laminado"));
        opciones.add(new CatalogModel("010", "Loseta Veneciana"));
        opciones.add(new CatalogModel("011", "Loseta Vinílica"));
        opciones.add(new CatalogModel("012", "Madera Machihembrada"));
        opciones.add(new CatalogModel("013", "Mármol"));
        opciones.add(new CatalogModel("014", "Parquet"));
        opciones.add(new CatalogModel("015", "Parquet de Primera"));
        opciones.add(new CatalogModel("016", "Parquet Fino"));
        opciones.add(new CatalogModel("017", "Porcelanato"));
        opciones.add(new CatalogModel("018", "Porcelanato Importado"));
        opciones.add(new CatalogModel("019", "Terrazo"));
        opciones.add(new CatalogModel("020", "Tierra Compactada"));
        opciones.add(new CatalogModel("021", "Otros"));

        setupMultiAutoCompleteTextView(multiCompletePisos, opciones, valoresBD);
    }
    private void loadDataPisosCocina(String valoresBD){
        List<CatalogModel> opciones = new ArrayList<>();
        opciones.add(new CatalogModel("001", "Alfombra"));
        opciones.add(new CatalogModel("002", "Bambú"));
        opciones.add(new CatalogModel("003", "Cemento Bruñado"));
        opciones.add(new CatalogModel("004", "Cemento Pulido"));
        opciones.add(new CatalogModel("005", "Cerámica"));
        opciones.add(new CatalogModel("006", "Cerámica Importada"));
        opciones.add(new CatalogModel("007", "Ladrillo Pastelero"));
        opciones.add(new CatalogModel("008", "Lajas"));
        opciones.add(new CatalogModel("009", "Laminado"));
        opciones.add(new CatalogModel("010", "Loseta Veneciana"));
        opciones.add(new CatalogModel("011", "Loseta Vinílica"));
        opciones.add(new CatalogModel("012", "Madera Machihembrada"));
        opciones.add(new CatalogModel("013", "Mármol"));
        opciones.add(new CatalogModel("014", "Mármol Importado"));
        opciones.add(new CatalogModel("015", "Parquet"));
        opciones.add(new CatalogModel("016", "Parquet de Primera"));
        opciones.add(new CatalogModel("017", "Parquet Fino"));
        opciones.add(new CatalogModel("018", "Porcelanato"));
        opciones.add(new CatalogModel("019", "Terrazo"));
        opciones.add(new CatalogModel("020", "Tierra Compactada"));
        opciones.add(new CatalogModel("021", "Otros"));

        setupMultiAutoCompleteTextView(multiCompletePisosCocina, opciones, valoresBD);
    }
    private void loadDataParedesCocina(String valoresBD){
        List<CatalogModel> opciones = new ArrayList<>();
        opciones.add(new CatalogModel("001", "Tarrajeo"));
        opciones.add(new CatalogModel("002", "Pintura"));
        opciones.add(new CatalogModel("003", "Tarrajeo y Pintura"));
        opciones.add(new CatalogModel("004", "Cerámico"));
        opciones.add(new CatalogModel("005", "Mayólica"));
        opciones.add(new CatalogModel("006", "Porcelanato"));
        opciones.add(new CatalogModel("007", "Concreto Visto"));
        opciones.add(new CatalogModel("008", "Ladrillo Caravista "));
        opciones.add(new CatalogModel("009", "Sin tarrajeo con Pintura"));
        opciones.add(new CatalogModel("010", "Papel mural "));
        opciones.add(new CatalogModel("011", "Otros"));

        setupMultiAutoCompleteTextView(multiCompleteParedesCocina, opciones, valoresBD);
    }
    private void loadDataPisosBaños(String valoresBD){
        List<CatalogModel> opciones = new ArrayList<>();
        opciones.add(new CatalogModel("001", "Alfombra"));
        opciones.add(new CatalogModel("002", "Bambú"));
        opciones.add(new CatalogModel("003", "Cemento Bruñado"));
        opciones.add(new CatalogModel("004", "Cemento Pulido"));
        opciones.add(new CatalogModel("005", "Cerámica"));
        opciones.add(new CatalogModel("006", "Cerámica Importada"));
        opciones.add(new CatalogModel("007", "Ladrillo Pastelero"));
        opciones.add(new CatalogModel("008", "Lajas"));
        opciones.add(new CatalogModel("009", "Laminado"));
        opciones.add(new CatalogModel("010", "Loseta Veneciana"));
        opciones.add(new CatalogModel("011", "Loseta Vinílica"));
        opciones.add(new CatalogModel("012", "Madera Machihembrada"));
        opciones.add(new CatalogModel("013", "Mármol"));
        opciones.add(new CatalogModel("014", "Mármol Importado"));
        opciones.add(new CatalogModel("015", "Parquet"));
        opciones.add(new CatalogModel("016", "Parquet de Primera"));
        opciones.add(new CatalogModel("017", "Parquet Fino"));
        opciones.add(new CatalogModel("018", "Porcelanato"));
        opciones.add(new CatalogModel("019", "Terrazo"));
        opciones.add(new CatalogModel("020", "Tierra Compactada"));
        opciones.add(new CatalogModel("021", "Otros"));

        setupMultiAutoCompleteTextView(multiCompletePisosBaños, opciones, valoresBD);
    }
    private void loadDataParedesBaños(String valoresBD){
        List<CatalogModel> opciones = new ArrayList<>();
        opciones.add(new CatalogModel("001", "Tarrajeo"));
        opciones.add(new CatalogModel("002", "Pintura"));
        opciones.add(new CatalogModel("003", "Tarrajeo y Pintura"));
        opciones.add(new CatalogModel("004", "Cerámico"));
        opciones.add(new CatalogModel("005", "Mayólica"));
        opciones.add(new CatalogModel("006", "Porcelanato"));
        opciones.add(new CatalogModel("007", "Concreto Visto"));
        opciones.add(new CatalogModel("008", "Ladrillo Caravista "));
        opciones.add(new CatalogModel("009", "Sin tarrajeo con Pintura"));
        opciones.add(new CatalogModel("010", "Papel mural "));
        opciones.add(new CatalogModel("011", "Otros"));

        setupMultiAutoCompleteTextView(multiCompleteParedesBaño, opciones, valoresBD);
    }

    private void setupMultiAutoCompleteTextView(MultiAutoCompleteTextView multiAuto,
                                                List<CatalogModel> opciones, String valoresBD) {
        Set<String> seleccionados = new LinkedHashSet<>();

        ArrayAdapter<CatalogModel> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>(opciones)
        );

        multiAuto.setAdapter(adapter);
        multiAuto.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        multiAuto.setOnClickListener(v -> multiAuto.showDropDown());
        multiAuto.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                multiAuto.showDropDown();
            }
        });

        if (valoresBD != null && !valoresBD.trim().isEmpty()) {
            String[] valoresSeleccionados = valoresBD.split(", ");
            for (String valor : valoresSeleccionados) {
                seleccionados.add(valor.trim());
            }
            String textoInicial = TextUtils.join(", ", seleccionados);
            if (!textoInicial.endsWith(", ")) {
                textoInicial += ", ";
            }
            multiAuto.setText(textoInicial);
            multiAuto.setSelection(multiAuto.getText().length());
        }

        multiAuto.setOnItemClickListener((parent, view, position, id) -> {
            CatalogModel seleccion = adapter.getItem(position);
            if (seleccion != null) {
                String selectedValue = seleccion.toString();
                if (!seleccionados.contains(selectedValue)) {
                    seleccionados.add(selectedValue);  // Agregar el nuevo valor
                    multiAuto.setText(TextUtils.join(", ", seleccionados) + ", ");  // Actualizar el texto con coma
                    multiAuto.setSelection(multiAuto.getText().length());  // Colocar el cursor al final
                }
            }
        });
        multiAuto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String[] valores = s.toString().split(", ");
                seleccionados.clear();

                for (String valor : valores) {
                    if (isInCatalog(valor.trim(), opciones)) {
                        seleccionados.add(valor.trim());
                    }
                }
            }
        });
    }
    private boolean isInCatalog(String value, List<CatalogModel> catalog) {
        for (CatalogModel item : catalog) {
            if (item.toString().equals(value)) {
                return true;
            }
        }
        return false;
    }
    private int getIndex(List<CatalogModel> modalidades, String valorBD) {
        for (int i = 0; i < modalidades.size(); i++) {
            if (modalidades.get(i).getDescripcion().equals(valorBD)) {
                return i;
            }
        }
        return -1;
    }

    private void loadDataTipoPuerta(String valorBD){
        List<CatalogModel> modalidades = new ArrayList<>();
        modalidades.add(new CatalogModel("00", "Seleccione una opción"));
        modalidades.add(new CatalogModel("01", "Apanelada"));
        modalidades.add(new CatalogModel("02", "Maciza"));
        modalidades.add(new CatalogModel("03", "Contraplacada"));
        modalidades.add(new CatalogModel("04", "Fierro-Vidrio"));
        modalidades.add(new CatalogModel("05", "Vidrio "));
        modalidades.add(new CatalogModel("06", "Antiruido"));
        modalidades.add(new CatalogModel("07", "Otros"));

        ArrayAdapter<CatalogModel> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, modalidades);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnTipoPuerta.setAdapter(adapter);

        if (valorBD != null && !valorBD.trim().isEmpty()) {
            int position = getIndex(modalidades, valorBD);
            spnTipoPuerta.setSelection(Math.max(position, 0));
        } else {
            spnTipoPuerta.setSelection(0);
        }
    }
    private void loadDataMaterialPuerta(String valorBD){
        List<CatalogModel> modalidades = new ArrayList<>();
        modalidades.add(new CatalogModel("00", "Seleccione una opción"));
        modalidades.add(new CatalogModel("01", "Fierro"));
        modalidades.add(new CatalogModel("02", "Madera"));
        modalidades.add(new CatalogModel("03", "Vidrio templado"));
        modalidades.add(new CatalogModel("04", "Madera - fierro"));
        modalidades.add(new CatalogModel("05", "Vidrio Laminado "));
        modalidades.add(new CatalogModel("06", "MDF Acabado al duco"));
        modalidades.add(new CatalogModel("07", "MDF laminado "));
        modalidades.add(new CatalogModel("08", "Fierro- Vidrio"));
        modalidades.add(new CatalogModel("09", "Otros"));

        ArrayAdapter<CatalogModel> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, modalidades);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnMaterialPuerta.setAdapter(adapter);

        if (valorBD != null && !valorBD.trim().isEmpty()) {
            int position = getIndex(modalidades, valorBD);
            spnMaterialPuerta.setSelection(Math.max(position, 0));
        } else {
            spnMaterialPuerta.setSelection(0);
        }
    }
    private void loadDataSistemaPuerta(String valorBD){
        List<CatalogModel> modalidades = new ArrayList<>();
        modalidades.add(new CatalogModel("00", "Seleccione una opción"));
        modalidades.add(new CatalogModel("01", "Corrediza"));
        modalidades.add(new CatalogModel("02", "Batiente"));
        modalidades.add(new CatalogModel("03", "Levadiza"));
        modalidades.add(new CatalogModel("04", "Enrollable"));
        modalidades.add(new CatalogModel("05", "Otros"));

        ArrayAdapter<CatalogModel> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, modalidades);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnSistemaPuerta.setAdapter(adapter);

        if (valorBD != null && !valorBD.trim().isEmpty()) {
            int position = getIndex(modalidades, valorBD);
            spnSistemaPuerta.setSelection(Math.max(position, 0));
        } else {
            spnSistemaPuerta.setSelection(0);
        }
    }
    private void loadDataMarcoVentana(String valorBD){
        List<CatalogModel> modalidades = new ArrayList<>();
        modalidades.add(new CatalogModel("00", "Seleccione una opción"));
        modalidades.add(new CatalogModel("01", "Madera"));
        modalidades.add(new CatalogModel("02", "Aluminio"));
        modalidades.add(new CatalogModel("03", "Fierro"));
        modalidades.add(new CatalogModel("04", "Aluminio / Madera"));
        modalidades.add(new CatalogModel("05", "Otros"));

        ArrayAdapter<CatalogModel> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, modalidades);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnMarcoVentana.setAdapter(adapter);

        if (valorBD != null && !valorBD.trim().isEmpty()) {
            int position = getIndex(modalidades, valorBD);
            spnMarcoVentana.setSelection(Math.max(position, 0));
        } else {
            spnMarcoVentana.setSelection(0);
        }
    }
    private void loadDataVidrioVentana(String valorBD){
        List<CatalogModel> modalidades = new ArrayList<>();
        modalidades.add(new CatalogModel("00", "Seleccione una opción"));
        modalidades.add(new CatalogModel("01", "Crudo"));
        modalidades.add(new CatalogModel("02", "Templado"));
        modalidades.add(new CatalogModel("03", "Semitemplado"));
        modalidades.add(new CatalogModel("04", "Laminado"));
        modalidades.add(new CatalogModel("05", "Crudo Oscuro"));
        modalidades.add(new CatalogModel("06", "Otros"));


        ArrayAdapter<CatalogModel> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, modalidades);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnVidrioVentana.setAdapter(adapter);

        if (valorBD != null && !valorBD.trim().isEmpty()) {
            int position = getIndex(modalidades, valorBD);
            spnVidrioVentana.setSelection(Math.max(position, 0));
        } else {
            spnVidrioVentana.setSelection(0);
        }
    }
    private void loadDataSistemaVentana(String valorBD){
        List<CatalogModel> modalidades = new ArrayList<>();
        modalidades.add(new CatalogModel("00", "Seleccione una opción"));
        modalidades.add(new CatalogModel("01", "Corrediza"));
        modalidades.add(new CatalogModel("02", "Batiente"));
        modalidades.add(new CatalogModel("03", "Otros"));

        ArrayAdapter<CatalogModel> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, modalidades);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnSistemaVentana.setAdapter(adapter);

        if (valorBD != null && !valorBD.trim().isEmpty()) {
            int position = getIndex(modalidades, valorBD);
            spnSistemaVentana.setSelection(Math.max(position, 0));
        } else {
            spnSistemaVentana.setSelection(0);
        }
    }
    private void loadDataMarcoMampara(String valorBD){
        List<CatalogModel> modalidades = new ArrayList<>();
        modalidades.add(new CatalogModel("00", "Seleccione una opción"));
        modalidades.add(new CatalogModel("01", "Madera"));
        modalidades.add(new CatalogModel("02", "Aluminio"));
        modalidades.add(new CatalogModel("03", "Fierro"));
        modalidades.add(new CatalogModel("04", "Aluminio / Madera"));
        modalidades.add(new CatalogModel("05", "Otros"));

        ArrayAdapter<CatalogModel> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, modalidades);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnMarcoMampara.setAdapter(adapter);

        if (valorBD != null && !valorBD.trim().isEmpty()) {
            int position = getIndex(modalidades, valorBD);
            spnMarcoMampara.setSelection(Math.max(position, 0));
        } else {
            spnMarcoMampara.setSelection(0);
        }
    }
    private void loadDataVidrioMampara(String valorBD){
        List<CatalogModel> modalidades = new ArrayList<>();
        modalidades.add(new CatalogModel("00", "Seleccione una opción"));
        modalidades.add(new CatalogModel("01", "Crudo"));
        modalidades.add(new CatalogModel("02", "Templado"));
        modalidades.add(new CatalogModel("03", "Semitemplado"));
        modalidades.add(new CatalogModel("04", "Laminado"));
        modalidades.add(new CatalogModel("05", "Crudo Oscuro"));
        modalidades.add(new CatalogModel("06", "Otros"));

        ArrayAdapter<CatalogModel> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, modalidades);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnVidrioMampara.setAdapter(adapter);

        if (valorBD != null && !valorBD.trim().isEmpty()) {
            int position = getIndex(modalidades, valorBD);
            spnVidrioMampara.setSelection(Math.max(position, 0));
        } else {
            spnVidrioMampara.setSelection(0);
        }
    }
    private void loadDataSistemasMampara(String valorBD){
        List<CatalogModel> modalidades = new ArrayList<>();
        modalidades.add(new CatalogModel("00", "Seleccione una opción"));
        modalidades.add(new CatalogModel("01", "Corrediza"));
        modalidades.add(new CatalogModel("02", "Batiente"));
        modalidades.add(new CatalogModel("03", "Otros"));

        ArrayAdapter<CatalogModel> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, modalidades);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnSistemaMampara.setAdapter(adapter);

        if (valorBD != null && !valorBD.trim().isEmpty()) {
            int position = getIndex(modalidades, valorBD);
            spnSistemaMampara.setSelection(Math.max(position, 0));
        } else {
            spnSistemaMampara.setSelection(0);
        }
    }
    private void loadDataMueblesCocina(String valorBD){
        List<CatalogModel> modalidades = new ArrayList<>();
        modalidades.add(new CatalogModel("00", "Seleccione una opción"));
        modalidades.add(new CatalogModel("01", "Bajos"));
        modalidades.add(new CatalogModel("02", "Altos"));
        modalidades.add(new CatalogModel("03", "Altos y Bajos"));
        modalidades.add(new CatalogModel("04", "No Posee"));
        modalidades.add(new CatalogModel("05", "Otros"));

        ArrayAdapter<CatalogModel> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, modalidades);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnMuebleCocina.setAdapter(adapter);

        if (valorBD != null && !valorBD.trim().isEmpty()) {
            int position = getIndex(modalidades, valorBD);
            spnMuebleCocina.setSelection(Math.max(position, 0));
        } else {
            spnMuebleCocina.setSelection(0);
        }
    }
    private void loadDataMueblesCocina2(String valorBD){
        List<CatalogModel> modalidades = new ArrayList<>();
        modalidades.add(new CatalogModel("00", "Seleccione una opción"));
        modalidades.add(new CatalogModel("01", "Concreto"));
        modalidades.add(new CatalogModel("02", "Melamine"));
        modalidades.add(new CatalogModel("03", "MDF"));
        modalidades.add(new CatalogModel("04", "Madera"));
        modalidades.add(new CatalogModel("05", "Otros"));

        ArrayAdapter<CatalogModel> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, modalidades);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnMuebleCocina2.setAdapter(adapter);

        if (valorBD != null && !valorBD.trim().isEmpty()) {
            int position = getIndex(modalidades, valorBD);
            spnMuebleCocina2.setSelection(Math.max(position, 0));
        } else {
            spnMuebleCocina2.setSelection(0);
        }
    }
    private void loadDataTablero(String valorBD){
        List<CatalogModel> modalidades = new ArrayList<>();
        modalidades.add(new CatalogModel("00", "Seleccione una opción"));
        modalidades.add(new CatalogModel("01", "Madera"));
        modalidades.add(new CatalogModel("02", "Granito"));
        modalidades.add(new CatalogModel("03", "Mármol"));
        modalidades.add(new CatalogModel("04", "Albañilería"));
        modalidades.add(new CatalogModel("05", "Postformado"));
        modalidades.add(new CatalogModel("06", "Cerámica/Mayólica"));
        modalidades.add(new CatalogModel("07", "Silestone"));
        modalidades.add(new CatalogModel("08", "Laminados"));
        modalidades.add(new CatalogModel("09", "Otros"));

        ArrayAdapter<CatalogModel> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, modalidades);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnTablero.setAdapter(adapter);

        if (valorBD != null && !valorBD.trim().isEmpty()) {
            int position = getIndex(modalidades, valorBD);
            spnTablero.setSelection(Math.max(position, 0));
        } else {
            spnTablero.setSelection(0);
        }
    }
    private void loadDataLavadero(String valorBD){
        List<CatalogModel> modalidades = new ArrayList<>();
        modalidades.add(new CatalogModel("00", "Seleccione una opción"));
        modalidades.add(new CatalogModel("01", "Acero"));
        modalidades.add(new CatalogModel("02", "Fierro Enlozado"));
        modalidades.add(new CatalogModel("03", "Otros"));

        ArrayAdapter<CatalogModel> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, modalidades);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnLavaderos.setAdapter(adapter);

        if (valorBD != null && !valorBD.trim().isEmpty()) {
            int position = getIndex(modalidades, valorBD);
            spnLavaderos.setSelection(Math.max(position, 0));
        } else {
            spnLavaderos.setSelection(0);
        }
    }
    private void loadDataTipoSanitarios(String valorBD){
        List<CatalogModel> modalidades = new ArrayList<>();
        modalidades.add(new CatalogModel("00", "Seleccione una opción"));
        modalidades.add(new CatalogModel("01", "Sifón Top Piece y/o Rapid Jet"));
        modalidades.add(new CatalogModel("02", "Inodoros c / fluxometro"));
        modalidades.add(new CatalogModel("03", "One Piece, lavatorio "));
        modalidades.add(new CatalogModel("04", "Con tableros de mármol"));
        modalidades.add(new CatalogModel("05", "Con aparatos de vidrio( ovalin )"));
        modalidades.add(new CatalogModel("06", "Inodoro Suspendido "));
        modalidades.add(new CatalogModel("07", "One Piece, ovalin, tina."));
        modalidades.add(new CatalogModel("08", "One Piece, ovalin, ducha "));
        modalidades.add(new CatalogModel("09", "Otros"));

        ArrayAdapter<CatalogModel> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, modalidades);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnSanitarioTipo.setAdapter(adapter);

        if (valorBD != null && !valorBD.trim().isEmpty()) {
            int position = getIndex(modalidades, valorBD);
            spnSanitarioTipo.setSelection(Math.max(position, 0));
        } else {
            spnSanitarioTipo.setSelection(0);
        }
    }
    private void loadDataColorSanitarios(String valorBD){
        List<CatalogModel> modalidades = new ArrayList<>();
        modalidades.add(new CatalogModel("00", "Seleccione una opción"));
        modalidades.add(new CatalogModel("01", "Blanco"));
        modalidades.add(new CatalogModel("02", "Color"));
        modalidades.add(new CatalogModel("03", "Incoloro"));
        modalidades.add(new CatalogModel("04", "Otros"));

        ArrayAdapter<CatalogModel> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, modalidades);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnSanitarioColor.setAdapter(adapter);

        if (valorBD != null && !valorBD.trim().isEmpty()) {
            int position = getIndex(modalidades, valorBD);
            spnSanitarioColor.setSelection(Math.max(position, 0));
        } else {
            spnSanitarioColor.setSelection(0);
        }
    }
    private void loadDataSanitarios(String valorBD){
        List<CatalogModel> modalidades = new ArrayList<>();
        modalidades.add(new CatalogModel("00", "Seleccione una opción"));
        modalidades.add(new CatalogModel("01", "Importada"));
        modalidades.add(new CatalogModel("02", "Nacional"));
        modalidades.add(new CatalogModel("03", "Estándar"));
        modalidades.add(new CatalogModel("04", "Presurizada"));
        modalidades.add(new CatalogModel("05", "Automática"));
        modalidades.add(new CatalogModel("06", "Otra"));

        ArrayAdapter<CatalogModel> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, modalidades);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnSanitario.setAdapter(adapter);

        if (valorBD != null && !valorBD.trim().isEmpty()) {
            int position = getIndex(modalidades, valorBD);
            spnSanitario.setSelection(Math.max(position, 0));
        } else {
            spnSanitario.setSelection(0);
        }
    }
    private void loadDataISS(String valorBD){
        List<CatalogModel> modalidades = new ArrayList<>();
        modalidades.add(new CatalogModel("00", "Seleccione una opción"));
        modalidades.add(new CatalogModel("01", "Sin Instalación"));
        modalidades.add(new CatalogModel("02", "Tuberías de agua fría y desagüe"));
        modalidades.add(new CatalogModel("03", "Tuberías de agua fría, caliente y desagüe"));
        modalidades.add(new CatalogModel("04", "Sist. de Bombeo de agua potable y desagüe"));
        modalidades.add(new CatalogModel("05", "Sist. de Bombeo de agua potable y desagüe por bombeo"));
        modalidades.add(new CatalogModel("06", "Otra"));

        ArrayAdapter<CatalogModel> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, modalidades);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnIss.setAdapter(adapter);

        if (valorBD != null && !valorBD.trim().isEmpty()) {
            int position = getIndex(modalidades, valorBD);
            spnIss.setSelection(Math.max(position, 0));
        } else {
            spnIss.setSelection(0);
        }
    }
    private void loadDataIIEE(String valorBD){
        List<CatalogModel> modalidades = new ArrayList<>();
        modalidades.add(new CatalogModel("00", "Seleccione una opción"));
        modalidades.add(new CatalogModel("01", "Sin Instalación"));
        modalidades.add(new CatalogModel("02", "Corriente Monofásica sin empotrar"));
        modalidades.add(new CatalogModel("03", "Corriente Monofásica empotrada"));
        modalidades.add(new CatalogModel("04", "Corriente Trifásica"));
        modalidades.add(new CatalogModel("05", "Corriente Trifásica empotrada"));
        modalidades.add(new CatalogModel("07", "Corriente Trifásica sin empotrada"));
        modalidades.add(new CatalogModel("08", "Con sub estación eléctrica"));
        modalidades.add(new CatalogModel("09", "Otros"));

        ArrayAdapter<CatalogModel> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, modalidades);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnIiee.setAdapter(adapter);

        if (valorBD != null && !valorBD.trim().isEmpty()) {
            int position = getIndex(modalidades, valorBD);
            spnIiee.setSelection(Math.max(position, 0));
        } else {
            spnIiee.setSelection(0);
        }
    }

    private boolean validateSpinner(Spinner spinner, String defaultValue, Drawable errorBackground, Drawable defaultBackground) {
        if (spinner.getSelectedItem().toString().equals(defaultValue)) {
            spinner.setBackground(errorBackground);
            return false;
        } else {
            spinner.setBackground(defaultBackground);
            return true;
        }
    }
    private boolean validateMultiAutoComplete(MultiAutoCompleteTextView multiAutoComplete, Drawable errorBackground, Drawable defaultBackground) {
        if (multiAutoComplete.getText().toString().trim().isEmpty()) {
            multiAutoComplete.setBackground(errorBackground);
            return false;
        } else {
            multiAutoComplete.setBackground(defaultBackground);
            return true;
        }
    }

    private boolean validarCampos() {
        String defaultValue = "Seleccione una opción";
        boolean isValid = true;

        Drawable errorBackground = ContextCompat.getDrawable(this, R.drawable.error_border);
        Drawable defaultBackground = ContextCompat.getDrawable(this, R.drawable.default_border);

        List<Spinner> spinners = Arrays.asList(
                spnTipoPuerta, spnMaterialPuerta, spnSistemaPuerta,
                spnMarcoVentana, spnVidrioVentana, spnSistemaVentana,
                spnMuebleCocina, spnMuebleCocina2, spnTablero, spnLavaderos,
                spnSanitarioTipo, spnSanitarioColor, spnSanitario, spnIss, spnIiee
        );

        for (Spinner spinner : spinners) {
            if (!validateSpinner(spinner, defaultValue, errorBackground, defaultBackground)) {
                isValid = false;
            }
        }

        List<MultiAutoCompleteTextView> multiAutoCompletes = Arrays.asList(
                multiAutoCompleteTextView, multiCompleteMuros, multiCompleteRevestimiento,
                multiCompletePisos, multiCompletePisosCocina, multiCompleteParedesCocina,
                multiCompletePisosBaños, multiCompleteParedesBaño
        );

        // Validar MultiAutoCompleteTextViews
        for (MultiAutoCompleteTextView multiAutoComplete : multiAutoCompletes) {
            if (!validateMultiAutoComplete(multiAutoComplete, errorBackground, defaultBackground)) {
                isValid = false;
            }
        }

        if (radioGroupSistemaIncendio.getCheckedRadioButtonId() == -1) {
            isValid = false;
            tv_radioGroup.setBackground(errorBackground); // Aplica el borde de error
        } else {
            tv_radioGroup.setBackground(defaultBackground); // Aplica el borde por defecto
        }
        return isValid;
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

                    CaracteristicasEdificacion datos = new CaracteristicasEdificacion();

                    datos.setOtroEstructura(edtOtroEstructura.getText().toString());
                    datos.setOtroAlbañeria(edtOtroAlbañeria.getText().toString());
                    datos.setOtroPisos(edtOtroPisos.getText().toString());
                    datos.setOtroPuertas(edtOtroPuertas.getText().toString());
                    datos.setOtroVentana(edtOtroVentana.getText().toString());
                    datos.setOtroMampara(edtOtroMampara.getText().toString());
                    datos.setOtroCocina(edtOtroCocina.getText().toString());
                    datos.setOtroBaño(edtOtroBaño.getText().toString());
                    datos.setOtroSanitarias(edtOtroSanitarias.getText().toString());
                    datos.setOtroElectricas(edtOtroElectricas.getText().toString());

                    datos.setTipoPuerta(spnTipoPuerta.getSelectedItem().toString());
                    datos.setMaterialPuerta(spnMaterialPuerta.getSelectedItem().toString());
                    datos.setSistemaPuerta(spnSistemaPuerta.getSelectedItem().toString());
                    datos.setMarcoVentana(spnMarcoVentana.getSelectedItem().toString());
                    datos.setVidrioVentana(spnVidrioVentana.getSelectedItem().toString());
                    datos.setSistemaVentana(spnSistemaVentana.getSelectedItem().toString());
                    datos.setMarcoMampara(spnMarcoMampara.getSelectedItem().toString());
                    datos.setVidrioMampara(spnVidrioMampara.getSelectedItem().toString());
                    datos.setSistemaMampara(spnSistemaMampara.getSelectedItem().toString());
                    datos.setMuebleCocina(spnMuebleCocina.getSelectedItem().toString());
                    datos.setMuebleCocina2(spnMuebleCocina2.getSelectedItem().toString());
                    datos.setTablero(spnTablero.getSelectedItem().toString());
                    datos.setLavaderos(spnLavaderos.getSelectedItem().toString());
                    datos.setSanitarioTipo(spnSanitarioTipo.getSelectedItem().toString());
                    datos.setSanitarioColor(spnSanitarioColor.getSelectedItem().toString());
                    datos.setSanitario(spnSanitario.getSelectedItem().toString());
                    datos.setIss(spnIss.getSelectedItem().toString());
                    datos.setIiee(spnIiee.getSelectedItem().toString());

                    if (radioTiene.isChecked()) {
                        datos.setSistemaIncendio("Tiene");
                    } else if (radioNoTiene.isChecked()) {
                        datos.setSistemaIncendio("No tiene");
                    }

                    datos.setEstructura(multiAutoCompleteTextView.getText().toString());
                    datos.setMuros(multiCompleteMuros.getText().toString());
                    datos.setRevestimiento(multiCompleteRevestimiento.getText().toString());
                    datos.setPisos(multiCompletePisos.getText().toString());
                    datos.setPisosCocina(multiCompletePisosCocina.getText().toString());
                    datos.setParedesCocina(multiCompleteParedesCocina.getText().toString());
                    datos.setPisosBaños(multiCompletePisosBaños.getText().toString());
                    datos.setParedesBaño(multiCompleteParedesBaño.getText().toString());

                    daoExtras.actualizarRegistroCaracEdificacion(datos, inspeccion.getNumInspeccion());

                    Intent intent = new Intent(RegistroCaracteristicasEdificacionActivity.this, RegistroCaractInfraestruturaActivity.class);
                    intent.putExtra("inspeccion", inspeccion);
                    intent.putExtra("caracteristicasGenerales", caracteristicasGenerales);
                    intent.putExtra("caracteristicasEdificacion", datos);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(RegistroCaracteristicasEdificacionActivity.this);
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

        switch (requestCode) {
            case REQUEST_CODE_AGREGAR_PRODUCTO: {
                if (resultCode == RESULT_OK) {
                    AgregarProductoArgument argument = (AgregarProductoArgument) data.getSerializableExtra(
                            AgregarProductoActivity.EXTRA_PRODUCTO_AGREGADO);

                }
                break;
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
        AlertDialog.Builder builder = new AlertDialog.Builder(RegistroCaracteristicasEdificacionActivity.this);
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
