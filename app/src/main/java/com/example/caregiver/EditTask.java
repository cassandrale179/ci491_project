package com.example.caregiver;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.caregiver.model.Task;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EditTask extends AppCompatActivity {

    // fields in layout
    private EditText taskNameField;
    private EditText taskNotesField;
    private EditText caregiveeField;
    private Spinner roomSpinner;
    private TextView errorMessage;
    private ImageView imageView;

    private Task currTask; // current task being edited
    final DatabaseReference database = FirebaseDatabase.getInstance().getReference();

    private StorageReference storageReference;
    private Uri filePath;
    private static final int PICK_IMAGE_REQUEST = 234;
    private static final int CAPTURED_IMAGE_REQUEST = 1024;
    private String uploadingFolderFilename;
    private String uploadingFilename;
    android.app.AlertDialog.Builder builder;

    String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        // retrieve info from TaskFragment page intent
        Intent intent = getIntent();
        currTask = intent.getParcelableExtra("currTask");
        String[] caregiveeRooms = intent.getStringArrayExtra("rooms");
        String caregiveeName = intent.getStringExtra("caregiveeName");

        /* TODO better error handling */
        // if task is not found, do not display
        if(currTask == null){
            Log.e("FAIL", "EditTask:onCreate could not get selectedTask from TaskFragment.");
            return;
        }

        // get all field/spinners
        taskNameField = findViewById(R.id.taskName);
        taskNotesField = findViewById(R.id.taskNotes);
        roomSpinner = findViewById(R.id.taskRoom);
        caregiveeField = findViewById(R.id.taskCaregivee);
        errorMessage = findViewById(R.id.taskMessage);
        imageView = (ImageView) findViewById(R.id.addTaskLogo);

        // populate all fields with task info & create spinner
        taskNameField.setText(currTask.taskName, TextView.BufferType.EDITABLE);
        taskNotesField.setText(currTask.taskNote, TextView.BufferType.EDITABLE);
        caregiveeField.setText(caregiveeName, TextView.BufferType.NORMAL);
        createSpinner(caregiveeRooms, currTask.room);

        //Initialized the storage reference
        storageReference = FirebaseStorage.getInstance().getReference()
                .child(currTask.caregiverId.toString())
                .child(currTask.taskId.toString());
        //Populate the imageview with the associated image
        storageReference.getBytes(1024*1024*5)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        //Log.d("Success -123",storageReference.toString());
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                        imageView.setImageBitmap(bitmap);
                    }
                });

        //navigate to upload media
        TextView uploadMedia = findViewById(R.id.UploadMediaTextViewEditTaskView);
        //uploadMedia.setOnClickListener(view -> startActivity(new Intent(view.getContext(), UploadMedia.class)));

        builder = new android.app.AlertDialog.Builder(this);

        uploadMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Uncomment the below code to Set the message and title from the strings.xml file
                //builder.setMessage(R.string.dialog_message)
                // .setTitle(R.string.dialog_title);
                // add a list
                String[] options = {"Gallery", "Click"};
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) { //Gallery
                            showFileChooser();
                        } else if (which == 1) {//Click
                            dispatchTakePictureIntent();
                        }
                    }
                });
                //Creating dialog box
                android.app.AlertDialog alert = builder.create();
                //Setting the title manually
                alert.setTitle("Upload Image from Gallery or Click an Image");
                alert.show();
            }
        });

    }

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select an Image"), PICK_IMAGE_REQUEST);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) { //check if
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File...
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAPTURED_IMAGE_REQUEST);
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == CAPTURED_IMAGE_REQUEST) {
            File f = new File(currentPhotoPath);
            imageView.setImageURI(Uri.fromFile(f));
            filePath = Uri.fromFile(f);
            //Log.d("FILEPATH URI","Absolute URL of the image is " + Uri.fromFile(f));

        }
    }

    //this method will upload the file
    private void uploadFile(String taskuniqueID) {
        //if there is a file to upload
        Log.e("Filepath",filePath.toString());
        if (filePath != null) {
            storageReference = FirebaseStorage.getInstance().getReference();
            //displaying a progress dialog while upload is going on
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading");
            progressDialog.show();

            uploadingFolderFilename = currTask.caregiverId.toString();


            uploadingFilename = uploadingFolderFilename+("/")+taskuniqueID;
            //Log.d("Tag","UploadingFilename"+uploadingFilename);

            StorageReference riversRef = storageReference.child(uploadingFilename);
            riversRef.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //if the upload is successfull
                            //hiding the progress dialog
                            progressDialog.dismiss();

                            //and displaying a success toast
                            Toast.makeText(getApplicationContext(), "File Uploaded ", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            //if the upload is not successfull
                            //hiding the progress dialog
                            progressDialog.dismiss();
                            //and displaying error message
                            Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            //calculating progress percentage
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                            //displaying percentage in progress dialog
                            progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                        }
                    });
        }
        //if there is not any file
        else {
            Toast.makeText(getApplicationContext(), "File Not Uploaded ", Toast.LENGTH_LONG).show();
            //you can display an error toast
        }
    }

    /**
     * Initializes room spinner, creates adapter with list of all caregivee rooms
     * @param allCaregiveeRooms, list of caregivee rooms
     * @param currRoom, room of current task
     */
    protected void createSpinner(String[] allCaregiveeRooms, String currRoom){
        // swap first with curr room
        int currentRoomIndex = Arrays.asList(allCaregiveeRooms).indexOf(currRoom);
        String temp = allCaregiveeRooms[currentRoomIndex];
        allCaregiveeRooms[currentRoomIndex] = allCaregiveeRooms[0];
        allCaregiveeRooms[0] = temp;

        // Render list on the caregivee spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<String> (
                this, android.R.layout.simple_spinner_item,  allCaregiveeRooms);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
       roomSpinner.setAdapter(adapter);
    }

    /**
     * Back Button onClick function - navigates back to dashboard
     * @param view
     */
    public void navigateToDashboard(View view){
        // navigate to dashboard after update
        Intent intent = new Intent(this, Dashboard.class);
        startActivity(intent);
    }

    /**
     * Updates task in firebase, triggered when user clicks 'Update'
     * If room is updated, the previous task is removed
     * The updated task is written to the DB
     * @param view
     */
    public void updateTask(View view) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String path;
        String updatedRoom = roomSpinner.getSelectedItem().toString();

        // if room changed, remove the old task from its room
        if(!updatedRoom.equals(currTask.room)){
            path = createPath(currTask.caregiveeId, currTask.room, currTask.taskId);
            removeTaskInFirebase(path);
        }

        // post updated task details
        String updatedTaskName = String.valueOf(taskNameField.getText());
        String updatedNotes = String.valueOf(taskNotesField.getText());
        // formulate path
        path = createPath(currTask.caregiveeId, updatedRoom, currTask.taskId);
        // create updated task HashMap
        Map<String, Object> updatedTask = new HashMap<>();
        updatedTask.put("assignedStatus", currTask.assignedStatus); // prev value
        updatedTask.put("caregiverID", preferences.getString("userId", null)); // curr user's info
        updatedTask.put("completionStatus", currTask.completionStatus); // prev value
        updatedTask.put("name", updatedTaskName); // updated value
        updatedTask.put("notes", updatedNotes); // updated value

        // post updated task to path in Firebase
        updateTaskInFirebase(path, updatedTask);
        uploadFile(currTask.taskId.toString());
    }

    /**
     * Removes Task from Firebase after alerting user to confirm action
     * @param view,
     */
    public void deleteTask(View view){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setMessage("Would you like to delete this task?");
                alertDialog.setNegativeButton("Cancel", null);
                alertDialog.setPositiveButton("Delete Task", (dialog, which) -> {
                    String path = createPath(currTask.caregiveeId, currTask.room, currTask.taskId);
                    removeTaskInFirebase(path);
                    int green = ContextCompat.getColor(getApplicationContext(), R.color.green);
                    StorageReference storageRef = FirebaseStorage.getInstance().getReference();

                    // Create a reference to the file to delete
                    StorageReference desertRef = storageRef.child(currTask.caregiverId.toString())
                            .child(currTask.taskId.toString());

// Delete the file
                    desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // File deleted successfull
                            Log.d("Deleted","The Image is delete"+ desertRef.toString());
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Uh-oh, an error occurred!
                        }
                    });
                    displayMessage("Task deleted.", green);
                    navigateToDashboard(view);
                });
        alertDialog.create();
        alertDialog.show();
    }

    /**
     * Creates path String for Firebase update/removal
     * @param caregiveeId, ID of caregivee
     * @param room, task room
     * @param taskId, task ID
     * @return string with path location
     */
    private String createPath(String caregiveeId, String room, String taskId){
        return "/users/" + caregiveeId + "/rooms/" + room + "/tasks/" + taskId + "/";
    }

    /**
     * Render the error and success message field.
     * @param sourceString The text message to be displayed.
     * @param color The color for the text message (red for error, green for success).
     */
    public void displayMessage(String sourceString, int color) {


        errorMessage.setText(Html.fromHtml(sourceString));
        errorMessage.setVisibility(View.VISIBLE);
        errorMessage.setTextColor(color);
    }

    /**
     * In the case of a room change, this removes the task from the previous room
     * @param path, path to task that needs to be removed
     */
    private void removeTaskInFirebase(String path){
        DatabaseReference ref = database.child(path);
        ref.removeValue();
    }

    /**
     * Creates a ref to the inputted path to task id in the DB & updates task details
     * @param path, path to taskId
     * @param updatedTask, HashMap with task details (assignedStatus, CaregiverID, CompletionStatus,
     *                     Name, Notes
     */
    private void updateTaskInFirebase(String path, Map<String, Object> updatedTask){
        // Set color for success/error messages
        int red = ContextCompat.getColor(getApplicationContext(), R.color.red);
        int green = ContextCompat.getColor(getApplicationContext(), R.color.green);

        DatabaseReference ref = database.child(path);
        ref.updateChildren(updatedTask, (databaseError, databaseReference) -> {
            if (databaseError == null) {
                displayMessage("Your task is updated", green);
                // navigate to dashboard after update
                Intent intent = new Intent(this, Dashboard.class);
                startActivity(intent);
            } else {
                displayMessage("Your task cannot be updated", red);
            }
        });
    }
}
