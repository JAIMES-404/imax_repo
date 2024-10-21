package com.imax.app.ui.foto;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.imax.app.App;
import com.imax.app.R;
import com.imax.app.data.api.XMSApi;
import com.imax.app.data.api.request.FotoRequest;
import com.imax.app.data.api.request.InspeccionRequest;
import com.imax.app.data.dao.DAOExtras;
import com.imax.app.intents.AntesInspeccion;
import com.imax.app.intents.SignatureView;
import com.imax.app.managers.DataBaseHelper;
import com.imax.app.managers.TablesHelper;
import com.imax.app.models.AsignacionModel;
import com.imax.app.models.CatalogModel;
import com.imax.app.ui.activity.RegistrarCaractGeneralesActivity;
import com.imax.app.ui.activity.RegistroInspeccionActivity;
import com.imax.app.ui.adapters.FilesAdapter;
import com.imax.app.ui.pedido.AgregarProductoActivity;
import com.imax.app.ui.pedido.AgregarProductoArgument;
import com.imax.app.utils.Constants;
import com.imax.app.utils.MyDetailDialog;
import com.imax.app.utils.UnauthorizedException;
import com.imax.app.utils.Util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
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

public class RegistroFotoInsertActivity extends AppCompatActivity implements FilesAdapter.OnFileRemoveListener{
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

    private Spinner spnAsignarNumero;

    private Button btnTakePhoto1;
    private Button btnTakePhoto2;
    private Button btnTakePhoto3;
    private Button btnTakePhoto4;
    private Button btnTakePhoto5;
    private Button btnTakePhoto6;
    private ImageView imgPreview1;
    private ImageView imgPreview2;
    private ImageView imgPreview3;
    private ImageView imgPreview4;
    private ImageView imgPreview5;
    private ImageView imgPreview6;
    private Uri imageUri;
    private static final int PICK_FILES_REQUEST = 2;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int MAX_PIXELS = 8 * 1000000; // 8 MB
    private int currentImageIndex = -1; // -1 indica que no hay imagen seleccionada
    private TextView tvFileName;
    private TextView tvImageSize;
    private Drawable defaultBackground;
    private Calendar calendar;
    private App app;
    private android.app.AlertDialog.Builder dialogBuilder;
    private android.app.AlertDialog dialog;

    Button btnOpenMaps;
    private ProgressBar progress;
    private DataBaseHelper dataBaseHelper;

    private Button btnBrowseFiles;
    private RecyclerView recyclerFiles;
    private FilesAdapter filesAdapter;

    private List<String> filePaths = new ArrayList<>();
    private List<String> fileBase64List = new ArrayList<>();
    private List<String> filesNameList = new ArrayList<>();
    private ArrayList<AsignacionModel> lista = new ArrayList<>();

    private String[] imagePaths = new String[6];
    private LinearLayout filesContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_foto_1);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Util.actualizarToolBar(getString(R.string.nuevo_registro), true, this, R.drawable.ic_action_back);
        progress = findViewById(R.id.progress);
        dataBaseHelper = DataBaseHelper.getInstance(getApplicationContext());

        app = (App) getApplicationContext();
        calendar = Calendar.getInstance();
        daoExtras = new DAOExtras(getApplicationContext());

        spnAsignarNumero = findViewById(R.id.spn_asignarNumero);
        btnTakePhoto1 = findViewById(R.id.btnTakePhoto1);
        imgPreview1 = findViewById(R.id.imgPreview1);
        btnTakePhoto2 = findViewById(R.id.btnTakePhoto2);
        imgPreview2 = findViewById(R.id.imgPreview2);
        btnTakePhoto3 = findViewById(R.id.btnTakePhoto3);
        imgPreview3 = findViewById(R.id.imgPreview3);
        btnTakePhoto4 = findViewById(R.id.btnTakePhoto4);
        imgPreview4 = findViewById(R.id.imgPreview4);
        btnTakePhoto5 = findViewById(R.id.btnTakePhoto5);
        imgPreview5 = findViewById(R.id.imgPreview5);
        btnTakePhoto6 = findViewById(R.id.btnTakePhoto6);
        imgPreview6 = findViewById(R.id.imgPreview6);

        filesContainer = findViewById(R.id.files_container);

        imgPreview1.setVisibility(View.GONE);
        btnBrowseFiles = findViewById(R.id.btn_browse_files);

        recyclerFiles = findViewById(R.id.recycler_files);

        btnTakePhoto1.setOnClickListener(v -> {
            setCurrentImageIndex(0);
            openCamera();
        });
        btnTakePhoto2.setOnClickListener(v -> {
            setCurrentImageIndex(1);
            openCamera();
        });
        btnTakePhoto3.setOnClickListener(v -> {
            setCurrentImageIndex(2);
            openCamera();
        });
        btnTakePhoto4.setOnClickListener(v -> {
            setCurrentImageIndex(3);
            openCamera();
        });
        btnTakePhoto5.setOnClickListener(v -> {
            setCurrentImageIndex(4);
            openCamera();
        });
        btnTakePhoto6.setOnClickListener(v -> {
            setCurrentImageIndex(5);
            openCamera();
        });

        imgPreview1.setVisibility(View.GONE);
        imgPreview2.setVisibility(View.GONE);
        imgPreview3.setVisibility(View.GONE);
        imgPreview4.setVisibility(View.GONE);
        imgPreview5.setVisibility(View.GONE);
        imgPreview6.setVisibility(View.GONE);

        new async_sincronizacion().execute();

        spnAsignarNumero.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("Item selected");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        filesNameList = new ArrayList<>();

        filesAdapter = new FilesAdapter(filesNameList, this);
        recyclerFiles.setLayoutManager(new LinearLayoutManager(this));
        recyclerFiles.setAdapter(filesAdapter);

        btnBrowseFiles.setOnClickListener(v -> openFilePicker());

        spnAsignarNumero.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //loadDataTipoInscripcion(modalidadSeleccionada);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        progressDialog = new ProgressDialog(RegistroFotoInsertActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        defaultBackground = ContextCompat.getDrawable(this, R.drawable.default_border);

        showAlertMaxMB("ADVENTENCIA", "Solo se acepta fotograficas con maximo de 8MB, de otro modo configurar resolución.");
    }

    private void setImageWhileIndex(int currentImageIndex, Bitmap imageBitmap){
        switch (currentImageIndex) {
            case 0:
                imgPreview1.setImageBitmap(imageBitmap);
                imgPreview1.setVisibility(View.VISIBLE);
                btnTakePhoto1.setText("Actualizar Foto");
                break;
            case 1:
                imgPreview2.setImageBitmap(imageBitmap);
                imgPreview2.setVisibility(View.VISIBLE);
                btnTakePhoto2.setText("Actualizar Foto");
                break;
            case 2:
                imgPreview3.setImageBitmap(imageBitmap);
                imgPreview3.setVisibility(View.VISIBLE);
                btnTakePhoto3.setText("Actualizar Foto");
                break;
            case 3:
                imgPreview4.setImageBitmap(imageBitmap);
                imgPreview4.setVisibility(View.VISIBLE);
                btnTakePhoto4.setText("Actualizar Foto");
                break;
            case 4:
                imgPreview5.setImageBitmap(imageBitmap);
                imgPreview5.setVisibility(View.VISIBLE);
                btnTakePhoto5.setText("Actualizar Foto");
                break;
            case 5:
                imgPreview6.setImageBitmap(imageBitmap);
                imgPreview6.setVisibility(View.VISIBLE);
                btnTakePhoto6.setText("Actualizar Foto");
                break;
            default:
                Toast.makeText(this, "Por favor, selecciona una imagen para actualizar", Toast.LENGTH_SHORT).show();
                break;
        }
    }
    private Bitmap decodeBase64ToBitmap(String base64Str) {
        byte[] decodedBytes = Base64.decode(base64Str, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
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
    void showAlertMaxMB(String titulo, String mensaje) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.ic_dialog_alert);
        builder.setTitle(titulo);
        builder.setMessage(mensaje);
        builder.setCancelable(false);
        builder.setNegativeButton("ACEPTAR", (dialog, which) -> {
            dialog.dismiss();
        });
        builder.show();
    }

    private void setCurrentImageIndex(int index) {
        this.currentImageIndex = index;
    }
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Seleccionar archivos"), PICK_FILES_REQUEST);
    }
    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
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
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            if (imageBitmap != null) {
                int width = imageBitmap.getWidth();
                int height = imageBitmap.getHeight();
                int totalPixels = width * height;

                if (totalPixels > MAX_PIXELS) {
                    Toast.makeText(this, "La imagen no debe superar los 8 MP", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    File photoFile = createImageFile();  // Crear archivo para la foto
                    if (photoFile != null) {
                        FileOutputStream fos = new FileOutputStream(photoFile);
                        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);  // Guardar la imagen
                        fos.flush();
                        fos.close();

                        byte[] imageBytes = Files.readAllBytes(photoFile.toPath());
                        String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                        String mimeType = getMimeType(photoFile.getAbsolutePath());
                        if (mimeType == null) {
                            mimeType = "application/octet-stream";
                        }
                        String fileName = photoFile.getName();
                        String combinedItem = "data:" + mimeType + ";name=" + fileName + ";index=" +
                                currentImageIndex + ";base64," + base64Image;

                        imagePaths[currentImageIndex] = combinedItem;

                        imageUri = Uri.fromFile(photoFile);
                        switch (currentImageIndex) {
                            case 0:
                                imgPreview1.setImageURI(imageUri);
                                imgPreview1.setVisibility(View.VISIBLE);
                                btnTakePhoto1.setText("Actualizar Foto");
                                break;
                            case 1:
                                imgPreview2.setImageURI(imageUri);
                                imgPreview2.setVisibility(View.VISIBLE);
                                btnTakePhoto2.setText("Actualizar Foto");
                                break;
                            case 2:
                                imgPreview3.setImageURI(imageUri);
                                imgPreview3.setVisibility(View.VISIBLE);
                                btnTakePhoto3.setText("Actualizar Foto");
                                break;
                            case 3:
                                imgPreview4.setImageURI(imageUri);
                                imgPreview4.setVisibility(View.VISIBLE);
                                btnTakePhoto4.setText("Actualizar Foto");
                                break;
                            case 4:
                                imgPreview5.setImageURI(imageUri);
                                imgPreview5.setVisibility(View.VISIBLE);
                                btnTakePhoto5.setText("Actualizar Foto");
                                break;
                            case 5:
                                imgPreview6.setImageURI(imageUri);
                                imgPreview6.setVisibility(View.VISIBLE);
                                btnTakePhoto6.setText("Actualizar Foto");
                                break;
                            default:
                                Toast.makeText(this, "Por favor, selecciona una imagen para actualizar", Toast.LENGTH_SHORT).show();
                                break;
                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error al guardar la imagen", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir   
        );
        return image;
    }

    private File createImageFileForIndex(int index) throws IOException {
        String imageFileName = "IMG_" + index + ".jpg";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(storageDir, imageFileName);
    }

    private static String getMimeType(String fileName) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(fileName);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    public static List<String> combineBase64AndFileNames(List<String> fileBase64List, List<String> filesList) {
        List<String> combined = new ArrayList<>();

        for (int i = 0; i < fileBase64List.size(); i++) {
            String base64 = fileBase64List.get(i);
            String fileName = filesList.get(i);

            String mimeType = getMimeType(fileName);

            String combinedItem = "data:" + mimeType + ";name=" + fileName + ";index=" + i + ";base64," + base64;
            combined.add(combinedItem);
        }
        return combined;
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

                List<String> selectedFiles = filesNameList;

                System.out.println("fileBase64List.count " + fileBase64List.size());
                System.out.println("selectedFiles.count " + selectedFiles.size());

                List<String> combinedList = combineBase64AndFileNames(fileBase64List, selectedFiles);

                FotoRequest fotoRequest = new FotoRequest();
                fotoRequest.setNumInspeccion(spnAsignarNumero.getSelectedItem().toString());
                fotoRequest.setFotoArray1(Arrays.asList(imagePaths));
                fotoRequest.setFotosArrayAdjunto1(combinedList);

                if(daoExtras.existeRegistroFoto(spnAsignarNumero.getSelectedItem().toString())){
                    daoExtras.actualizarRegistroInpeccionFoto(fotoRequest);
                }else{
                    daoExtras.crearRegistroFoto(fotoRequest);
                }

                Intent intent = new Intent(this, RegistroFotoInsert2Activity.class);
                intent.putExtra("numAsignacion", fotoRequest.getNumInspeccion());
                startActivity(intent);

                break;
            case R.id.menu_pedido_guardar:
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(RegistroFotoInsertActivity.this);
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

    class async_sincronizacion extends AsyncTask<Void, String, String> {

        protected void onPreExecute() {
            progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {

                if (Util.isConnectingToRed(RegistroFotoInsertActivity.this)) {
                    Log.d(TAG, "sincronizando datos...");
                    Response<ResponseBody> response;

                    String domain = "[[\"user_id.login\",\"=\",\"jose.lunarejo@imax.com.pe\"],[\"stage_id.name\",\"in\",[\"Inspección (Perito)\",\"Elaboración (Perito)\"]]]";

                    response = XMSApi.getApiEasyfact(RegistroFotoInsertActivity.this.getApplicationContext())
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
                    Toast.makeText(RegistroFotoInsertActivity.this, R.string.error_no_autorizado, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    MyDetailDialog myDetailDialog = new MyDetailDialog(RegistroFotoInsertActivity.this,
                            R.drawable.ic_dialog_alert, getString(R.string.oops), getString(R.string.error_sincronizacion), result);
                    myDetailDialog.show();
                    break;
            }
            refreshLista();
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
        filePaths.remove(position);
        filesAdapter.notifyItemRemoved(position);

        if (filesNameList.isEmpty()) {
            recyclerFiles.setVisibility(View.GONE);
        }

        Toast.makeText(this, "Archivo eliminado", Toast.LENGTH_SHORT).show();
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

        if (lista.size() != 0) {
            FotoRequest fotoRequest = daoExtras.getListFotoByNumero(lista.get(0).getNumber());
            if (fotoRequest.getFotosArrayAdjunto1() != null && !fotoRequest.getFotosArrayAdjunto1().isEmpty()) {
                fileBase64List.clear();
                filesNameList.clear();
                filePaths.clear();
                for (String file : fotoRequest.getFotosArrayAdjunto1()) {
                    int base64Start = file.indexOf(";base64,") + 8;
                    int nameStart = file.indexOf("name=") + 5;
                    int nameEnd = file.indexOf(";index=");

                    String fileName = file.substring(nameStart, nameEnd);
                    String base64Data = file.substring(base64Start);

                    filesNameList.add(fileName);
                    fileBase64List.add(base64Data);

                    filePaths.add("Rutas");
                }
            }
            if (fotoRequest.getFotoArray1() != null && !fotoRequest.getFotoArray1().isEmpty()) {
                for (String file : fotoRequest.getFotoArray1()) {
                    System.out.println("file -> " + file);
                    if (file == null || file.isEmpty()) {
                        continue;
                    }

                    int base64Start = file.indexOf(";base64,") + 8;
                    int nameStart = file.indexOf("name=") + 5;
                    int nameEnd = file.indexOf(";index=");
                    int indexStart = file.indexOf("index=") + 6;
                    int indexEnd = file.indexOf(";base64,");

                    String fileName = file.substring(nameStart, nameEnd);
                    int fileIndex = Integer.parseInt(file.substring(indexStart, indexEnd));
                    String base64Data = file.substring(base64Start);
                    Bitmap imageBitmap = decodeBase64ToBitmap(base64Data);

                    setImageWhileIndex(fileIndex, imageBitmap);

                    System.out.println("fileName -> " + fileName);
                }
            }
            showFiles();
        }
    }

    public void showLoader() {
        progressDialog.show();
    }

    public void hideLoader() {
        progressDialog.dismiss();
    }

    public void showDialogoPostEnvio(@StringRes int tituloRes, @StringRes int mensajeRes, @DrawableRes int icon) {
        AlertDialog.Builder builder = new AlertDialog.Builder(RegistroFotoInsertActivity.this);
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