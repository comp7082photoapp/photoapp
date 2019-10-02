package com.example.photoapp.ui.dashboard;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import androidx.lifecycle.ViewModelProviders;

import com.example.photoapp.MainActivity;
import com.example.photoapp.R;
import com.example.photoapp.SearchActivity;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.app.Activity.RESULT_OK;

public class DashboardFragment extends Fragment {
    private Button photoButton;
    private Button rightButton;
    private Button leftButton;
    private Button captionSaveButton;
    private Button searchButton;
    private TextView timeTextView;
    private ImageView imageView;
    private EditText captionEditText;
    private DashboardViewModel dashboardViewModel;
    private File picture;
    private MainActivity testActivity;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    String currentPhotoPath;
    static final int REQUEST_TAKE_PHOTO = 1;
    public static final int REQUEST_EVALUATE = 0x12;
    int currentIndex = 0;
    public ArrayList<String> pictureList = new ArrayList<String>();

    public SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd");
    public SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
    public SimpleDateFormat printFormat = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        getPictureList();
        dashboardViewModel =
                ViewModelProviders.of(this).get(DashboardViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        testActivity = (MainActivity) getActivity();

        searchButton = root.findViewById(R.id.searchBtn);
        photoButton = root.findViewById(R.id.photoButton);
        rightButton = root.findViewById(R.id.rightButton);
        leftButton = root.findViewById(R.id.leftButton);
        timeTextView = root.findViewById(R.id.timeTextView);
        imageView = root.findViewById(R.id.imageView);
        captionEditText = root.findViewById(R.id.captionEditText);
        captionSaveButton = root.findViewById(R.id.captionSaveButton);

        ViewTreeObserver vto = imageView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                imageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                if (pictureList.size() >0){
                    setPic(pictureList.get(currentIndex));
                }
            }
        });

        rightButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (currentIndex +1>=pictureList.size()-1){
                    currentIndex = pictureList.size()-1;
                }else {
                    currentIndex +=1;
                }
                setPic(pictureList.get(currentIndex));
            }
        });

        leftButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (currentIndex -1<0){
                    currentIndex = 0;
                }else{
                    currentIndex -=1;
                }
                setPic(pictureList.get(currentIndex));
            }
        });
        captionSaveButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String caption = captionEditText.getText().toString();

                changeName(caption);
            }
        });
        searchButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivityForResult(intent, REQUEST_TAKE_PHOTO);
            }
        });




        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Ensure that there's a camera activity to handle the intent
                if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        Uri photoURI = FileProvider.getUriForFile(getActivity(),
                                "com.example.android.fileprovider",
                                photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                    }
                }
            }
        });



        return root;
    }


    public void changeName(String capition){
        if (capition.isEmpty()){
            capition = "capition";
        }
        String filename = pictureList.get(currentIndex);
        String [] temp = null;
        temp = filename.split("_");
        temp[2] = capition;
        String newfilename = temp[0];
        for (int i=1;i<temp.length;i++) {
            newfilename = newfilename + "_" + temp[i];
        }

        File newFile = new File(newfilename);
        if (!newFile.exists()){
            File file = new File(filename);
            file.renameTo(newFile);
            pictureList.set(currentIndex,newfilename);
        }
        Log.println(Log.INFO,"info",filename);
        Log.println(Log.INFO,"info",newfilename);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Log.println(Log.INFO,"info","2");
            setPic(currentPhotoPath);
        }

        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode==2) {
            String caption = data.getStringExtra("caption");
            String from = data.getStringExtra("from");
            String to = data.getStringExtra("to");

            search(caption,from,to);
            Log.println(Log.INFO,"info",caption);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String time = format.format(new Date());
        String imageFileName = "JPEG_" + time + "_caption_";
        Log.println(Log.INFO,"info",imageFileName);
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        getActivity().sendBroadcast(mediaScanIntent);
    }

    private void search(String caption,String from,String to){
        ArrayList<String> temp = new ArrayList<String>();
        ArrayList<String> tempWithCaption = new ArrayList<String>();
        for(int i = 0; i<pictureList.size();i++){
            String name = pictureList.get(i).toLowerCase();
            String fileCaption = getCaptionByFileName(name);
            if (fileCaption.contains(caption.toLowerCase())){
                temp.add(pictureList.get(i));
            }
        }
        if (temp.size()!=0){
            pictureList = temp;
            currentPhotoPath = pictureList.get(0);
            currentIndex = 0;
        }else{
            Toast toast = Toast.makeText(getContext(),
                    "No result",
                    Toast.LENGTH_SHORT);

            toast.show();
        }

        try {
            format.setLenient(false);
            Date fromDate=dayFormat.parse(from);
            Date toDate=dayFormat.parse(to);



            for(int i = 0; i<temp.size();i++){
                String name = temp.get(i).toLowerCase();
                Date fileTime = getImageTimeByFileName(name);
                Log.println(Log.INFO,"info",toDate.toString());
                Log.println(Log.INFO,"info",fromDate.toString());
                if (fileTime.getTime() <= toDate.getTime() && fileTime.getTime() >=fromDate.getTime()){
                    tempWithCaption.add(temp.get(i));
                }
            }

            if (tempWithCaption.size()!=0){
                pictureList = tempWithCaption;
                currentPhotoPath = pictureList.get(0);
                currentIndex = 0;
            }else{
                Toast toast = Toast.makeText(getContext(),
                        "No result",
                        Toast.LENGTH_SHORT);

                toast.show();
            }


        } catch (ParseException e) {
        }



    }

    private void setPic(String currentPhotoPath) {
        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(currentPhotoPath, bmOptions);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);
        Log.println(Log.INFO,"info",currentPhotoPath);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap  = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        imageView.setImageBitmap(bitmap);

        timeTextView.setText(printFormat.format(getImageTimeByFileName(currentPhotoPath)));
        captionEditText.setText(getCaptionByFileName(currentPhotoPath));
    }


    public Date getImageTimeByFileName(String filename){
        String [] temp = null;
        temp = filename.split("_");
        try {
            Date date = format.parse(temp[1]);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return (new Date());
    }


    public String getCaptionByFileName(String filename){
        String [] temp = null;
        temp = filename.split("_");
        return temp[2];
    }
    public void getPictureList() {
        ArrayList<String> list = new ArrayList<String>();
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File[] allfiles = storageDir.listFiles();
        if (allfiles == null) {
            return;
        }
        for(int k = 0; k < allfiles.length; k++) {
            final File fi = allfiles[k];
            if(fi.isFile()) {
                int idx = fi.getPath().lastIndexOf(".");
                if (idx <= 0) {
                    continue;
                }
                String suffix = fi.getPath().substring(idx);
                if (suffix.toLowerCase().equals(".jpg") || suffix.toLowerCase().equals(".jpeg") || suffix.toLowerCase().equals(".bmp") || suffix.toLowerCase().equals(".png") || suffix.toLowerCase().equals(".gif") ) {
                    list.add(fi.getPath());
                }
            }
        }
        pictureList =  list;
    }

}