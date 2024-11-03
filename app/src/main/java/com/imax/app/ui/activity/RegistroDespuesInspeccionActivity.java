package com.imax.app.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import androidx.annotation.Nullable;

import com.imax.app.App;
import com.imax.app.R;
import com.imax.app.data.api.request.InspeccionRequest;
import com.imax.app.data.dao.DAOExtras;
import com.imax.app.intents.AntesInspeccion;
import com.imax.app.intents.CaracteristicasEdificacion;
import com.imax.app.intents.CaracteristicasGenerales;
import com.imax.app.intents.DespuesInspeccion;
import com.imax.app.intents.SignatureView;
import com.imax.app.ui.adapters.FilesAdapter;
import com.imax.app.utils.Util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RegistroDespuesInspeccionActivity extends AppCompatActivity implements FilesAdapter.OnFileRemoveListener{
    private final String TAG = getClass().getName();

    public static final int ACCION_NUEVO_REGISTRO = 1;

    private ProgressDialog progressDialog;
    private ViewPager mViewPager;
    private String infraestructura_comentario;

    private DAOExtras daoExtras;

    private App app;
    private android.app.AlertDialog.Builder dialogBuilder;
    private android.app.AlertDialog dialog;

    private AntesInspeccion inspeccion;
    private CaracteristicasGenerales caracteristicasGenerales;
    private CaracteristicasEdificacion caracteristicasEdificacion;

    TextView tv_radioGroup, tv_radioGroup2;
    RadioGroup radioGroup1, radioGroup2;
    RadioButton radioTiene, radioNoTiene, radioTiene2, radioNoTiene2;
    EditText edt_especificar, edt_especificar_2;

    private static final int PICK_FILES_REQUEST = 1;
    private Button btnBrowseFiles;
    private RecyclerView recyclerFiles;
    private FilesAdapter filesAdapter;
    private SignatureView signatureView;
    private LinearLayout filesContainer;

    private List<String> filePaths = new ArrayList<>();
    private List<String> fileBase64List = new ArrayList<>();
    private List<String> filesNameList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_despues_inspeccion);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Util.actualizarToolBar(getString(R.string.nuevo_registro), true, this, R.drawable.ic_action_back);

        inspeccion = (AntesInspeccion) getIntent().getSerializableExtra("inspeccion");
        caracteristicasGenerales = (CaracteristicasGenerales) getIntent().getSerializableExtra("caracteristicasGenerales");
        caracteristicasEdificacion = (CaracteristicasEdificacion) getIntent().getSerializableExtra("caracteristicasEdificacion");
        infraestructura_comentario = (String) getIntent().getSerializableExtra("infraestructura_comentario");

        app = (App) getApplicationContext();

        daoExtras = new DAOExtras(getApplicationContext());

        tv_radioGroup = findViewById(R.id.tv_rb1);
        radioGroup1 = findViewById(R.id.radioGroup_1);
        radioTiene = findViewById(R.id.radio_tiene);
        radioNoTiene = findViewById(R.id.radio_no_tiene);

        tv_radioGroup2 = findViewById(R.id.tv_rb2);
        radioGroup2 = findViewById(R.id.radioGroup_2);
        radioTiene2 = findViewById(R.id.radio_tiene_1);
        radioNoTiene2 = findViewById(R.id.radio_no_tiene_2);

        edt_especificar = findViewById(R.id.edt_especificar);
        edt_especificar_2 = findViewById(R.id.edt_especificar_2);

        btnBrowseFiles = findViewById(R.id.btn_browse_files);
        recyclerFiles = findViewById(R.id.recycler_files);
        filesContainer = findViewById(R.id.files_container);

        filesAdapter = new FilesAdapter(filesNameList,this);

        recyclerFiles.setLayoutManager(new LinearLayoutManager(this));
        recyclerFiles.setAdapter(filesAdapter);

        loadDataIfExists(inspeccion.getNumInspeccion());

        btnBrowseFiles.setOnClickListener(v -> openFilePicker());

        progressDialog = new ProgressDialog(RegistroDespuesInspeccionActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);

        radioGroup1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                //
                radioGroup1.setBackground(ContextCompat.getDrawable(group.getContext(), R.drawable.default_border));
            }
        });

        radioGroup2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // Cuando el usuario selecciona una opción, quita el borde rojo del tv_radioGroup
                radioGroup2.setBackground(ContextCompat.getDrawable(group.getContext(), R.drawable.default_border));
            }
        });
    }




    private void loadDataIfExists(String numero){
        InspeccionRequest inspeccionRequest =  daoExtras.getListAsignacionByNumero(numero);

        edt_especificar.setText(inspeccionRequest.getEspecificar());
        edt_especificar_2.setText(inspeccionRequest.getEspecificar2());

        String cbCoincideInformacion = inspeccionRequest.getCbCoincideInformacion();
        String cbDocumentacionSITU = inspeccionRequest.getCbDocumentacionSITU();

        if ("SI".equals(cbCoincideInformacion)) {
            radioTiene.setChecked(true);
            radioNoTiene.setChecked(false);
        } else if ("NO".equals(cbCoincideInformacion)) {
            radioNoTiene.setChecked(true);
            radioTiene.setChecked(false);
        } else {
            radioTiene.setChecked(false);
            radioNoTiene.setChecked(false);
        }
        if ("SI".equals(cbDocumentacionSITU)) {
            radioTiene2.setChecked(true);
            radioNoTiene2.setChecked(false);
        } else if ("NO".equals(cbDocumentacionSITU)) {
            radioNoTiene2.setChecked(true);
            radioTiene2.setChecked(false);
        } else {
            radioTiene2.setChecked(false);
            radioNoTiene2.setChecked(false);
        }

        if(inspeccionRequest.getFiles() != null){
            for (String file : inspeccionRequest.getFiles()) {
                int base64Start = file.indexOf(";base64,") + 8;
                int nameStart = file.indexOf("name=") + 5;
                int nameEnd = file.indexOf(";base64,");

                String base64Data = file.substring(base64Start);
                String fileName = file.substring(nameStart, nameEnd);

                filesNameList.add(fileName);
                fileBase64List.add(base64Data);
                filePaths.add("Rutas");
            }
            showFiles();
        }
    }

    private void removeFile(int index) {
        filesNameList.remove(index);
        fileBase64List.remove(index);

        showFiles();
    }

    private void showFiles() {
        filesContainer.removeAllViews();

        for (int i = 0; i < filesNameList.size(); i++) {
            final int index = i;

            View fileItemView = LayoutInflater.from(this).inflate(R.layout.item_cliente, filesContainer, false);
            TextView tvFileName = fileItemView.findViewById(R.id.tv_file_name);
            tvFileName.setText(filesNameList.get(i));

            ImageView imgDelete = fileItemView.findViewById(R.id.img_delete);
            imgDelete.setOnClickListener(v -> {
                removeFile(index);
            });

            filesContainer.addView(fileItemView);
        }
    }

    private void openFilePicker() {

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Seleccionar archivos"), PICK_FILES_REQUEST);
    }
    private boolean validarCampos(){
        boolean isValid = true;

        Drawable errorBackground = ContextCompat.getDrawable(this, R.drawable.error_border);
        Drawable defaultBackground = ContextCompat.getDrawable(this, R.drawable.default_border);

        if (radioGroup1.getCheckedRadioButtonId() == -1) {
            isValid = false;
            radioGroup1.setBackground(errorBackground);
        } else {
            radioGroup1.setBackground(defaultBackground);
        }
        if (radioGroup2.getCheckedRadioButtonId() == -1) {
            isValid = false;
            radioGroup2.setBackground(errorBackground);
        } else {
            radioGroup2.setBackground(defaultBackground);
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
                if(validarCampos()){
                    boolean tiene = radioTiene.isChecked();
                    boolean tiene2 = radioTiene2.isChecked();

                    String especificar = edt_especificar.getText().toString();
                    String especificar2 = edt_especificar_2.getText().toString();

                    List<String> selectedFiles = filesNameList; //nombreList

                    DespuesInspeccion despuesInspeccion = new DespuesInspeccion(tiene, tiene2, especificar, especificar2, selectedFiles);

                    List<String> combinedList = combineBase64AndFileNames(fileBase64List, filesNameList);
                    daoExtras.actualizarRegistroDespuesInspeccion(despuesInspeccion, inspeccion.getNumInspeccion(), combinedList);

                    Intent intent = new Intent(RegistroDespuesInspeccionActivity.this, RegistroDespuesInspeccionFirmaActivity.class);
                    intent.putExtra("despuesInspeccion", despuesInspeccion);
                    intent.putExtra("infraestructura_comentario", infraestructura_comentario);
                    intent.putExtra("inspeccion", inspeccion);
                    intent.putExtra("caracteristicasGenerales", caracteristicasGenerales);
                    intent.putExtra("caracteristicasEdificacion", caracteristicasEdificacion);
                    intent.putStringArrayListExtra("filesListName", new ArrayList<>(filesNameList));
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

    public static List<String> combineBase64AndFileNames(List<String> fileBase64List, List<String> filesList) {
        List<String> combined = new ArrayList<>();

        for (int i = 0; i < fileBase64List.size(); i++) {
            String base64 = fileBase64List.get(i);
            String fileName = filesList.get(i);

            String mimeType = getMimeType(fileName);
            String combinedItem = "data:" + mimeType + ";name=" + fileName + ";base64," + base64;
            combined.add(combinedItem);
        }
        return combined;
    }

    public static String getMimeType(String fileName) {
        if (fileName.endsWith(".png")) {
            return "image/png";
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (fileName.endsWith(".pdf")) {
            return "application/pdf";
        } else if (fileName.endsWith(".txt")) {
            return "text/plain";
        }
        return "application/octet-stream";
    }
    private String convertFileToBase64FromPath(String filePath) {
        try {
            File file = new File(filePath);
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] bytes = new byte[(int) file.length()];
            fileInputStream.read(bytes);
            fileInputStream.close();
            return Base64.encodeToString(bytes, Base64.DEFAULT);  // Convertir a Base64
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void DialogoConfirmacion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RegistroDespuesInspeccionActivity.this);
        builder.setTitle(getString(R.string.descartar_cambios));
        builder.setMessage(getString(R.string.se_perdera_cambios));
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
        filesContainer.removeAllViews();
        if (requestCode == PICK_FILES_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri fileUri = data.getClipData().getItemAt(i).getUri();
                        String fileName = getFileName(fileUri);
                        String fileBase64 = convertFileToBase64(fileUri);
                        String filePath = saveFileToInternalStorage(fileBase64, fileName);
                        if (filePath != null) {
                            filePaths.add(filePath);

                            File file = new File(filePath);
                            if (file.exists()) fileBase64List.add(fileBase64);
                        }
                        filesNameList.add(fileName);
                    }
                } else if (data.getData() != null) {
                    Uri fileUri = data.getData();
                    String fileName = getFileName(fileUri);
                    String fileBase64 = convertFileToBase64(fileUri);
                    String filePath = saveFileToInternalStorage(fileBase64, fileName);

                    if (filePath != null) {
                        filePaths.add(filePath);

                        File file = new File(filePath);
                        if (file.exists()) fileBase64List.add(fileBase64);
                    }
                    filesNameList.add(fileName);
                }

                if (!filesNameList.isEmpty()) {
                    recyclerFiles.setVisibility(View.VISIBLE);
                }
                filesAdapter.notifyDataSetChanged();
            }
        }
    }

    private String saveFileToInternalStorage(String base64Data, String fileName) {
        try {
            File file = new File(getFilesDir(), fileName);
            FileOutputStream fos = new FileOutputStream(file);
            byte[] decodedBytes = Base64.decode(base64Data, Base64.DEFAULT);
            fos.write(decodedBytes);
            fos.close();
            return file.getAbsolutePath();  // Retorna la ruta completa del archivo guardado
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressLint("Range")
    private String getFileName(Uri uri) {
        String fileName = "";
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            cursor.close();
        }
        return fileName;
    }

    private String convertFileToBase64(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();

            byte[] fileBytes = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(fileBytes, Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onFileRemoved(int position) {
        filesNameList.remove(position);
        fileBase64List.remove(position);
        filePaths.remove(position);

        filesAdapter.notifyItemRemoved(position);
        if (filesNameList.isEmpty()) {
            recyclerFiles.setVisibility(View.GONE);
        }

        Toast.makeText(this, "Archivo eliminado", Toast.LENGTH_SHORT).show();
    }

    public void showLoader() {
        progressDialog.show();
    }

    public void hideLoader() {
        progressDialog.dismiss();
    }

    public void showDialogoPostEnvio(@StringRes int tituloRes, @StringRes int mensajeRes, @DrawableRes int icon) {
        AlertDialog.Builder builder = new AlertDialog.Builder(RegistroDespuesInspeccionActivity.this);
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
