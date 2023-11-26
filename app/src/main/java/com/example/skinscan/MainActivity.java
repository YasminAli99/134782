package com.example.skinscan;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.skinscan.ml.Model;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class MainActivity extends AppCompatActivity {

    Button camera, gallery , logout;
    ImageView imageView;
    TextView result;
    int imageSize = 40;

    int numClassesToDisplay = 8; // Replace 10 with the actual number you want

    int[] topClassIndices = new int[8]; // Replace someSize with the actual size you need


    String[] confidences = new String[numClassesToDisplay];




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        camera = findViewById(R.id.button);
        gallery = findViewById(R.id.button2);

        result = findViewById(R.id.result);
        imageView = findViewById(R.id.imageView);

        Button logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle logout functionality here
                performLogout();
            }
        });

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, 3);
                } else {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
                }
            }
        });
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cameraIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(cameraIntent, 1);
            }
        });
    }


 public void classifyImage(Bitmap image){
      try {
     Model model = Model.newInstance(getApplicationContext());

   //Creates inputs for reference.
   TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 40, 40, 3}, DataType.FLOAT32);
   //Process the input image and fill the input tensor
   ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
   byteBuffer.order(ByteOrder.nativeOrder());

  int[] intValues = new int[imageSize * imageSize];
  image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
  int pixel = 0;
//iterate over each pixel and extract R, G, and B values. Add those values individually to the byte buffer.
  for(int i = 0; i < imageSize; i ++){
 for(int j = 0; j < imageSize; j++){
  int val = intValues[pixel++]; // RGB
byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 1));
byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 1));
byteBuffer.putFloat((val & 0xFF) * (1.f / 1));
  }
}

  inputFeature0.loadBuffer(byteBuffer);

   //Runs model inference and gets result.
   Model.Outputs outputs = model.process(inputFeature0);
   TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

 float[] confidences = outputFeature0.getFloatArray();

 //Display the top N classes with the highest confidence score
          int numClassesToDisplay = 1;
          int[] topClassIndices = getTopNClassIndices(confidences, numClassesToDisplay);
// find the index of the class with the biggest confidence.
  //int maxPos = 0;
  //float maxConfidence = 0;
  // for (int i = 0; i < confidences.length; i++) {
  // if (confidences[i] > maxConfidence) {
  // maxConfidence = confidences[i];
  //maxPos = i;
  //  }

 String[] classes = {"BA- cellulitis", "BA-impetigo", "FU-athlete-foot","FU-nail-fungus", "FU-ringworm", "PA-cutaneous-larva-migrans", "VI-chickenpox", "VI-shingles"};
         // float[] confidences = new float[numClassesToDisplay];
          StringBuilder resultText = new StringBuilder();

     for (int i = 0; i < numClassesToDisplay; i++) {
         int index = topClassIndices[i];
         float confidence = confidences[index];  // Assign the confidence value

         resultText.append(classes[index]).append(": ").append((int) (confidences[index] * 100)).append("%\n");

     }

     result.setText(resultText.toString());
  //result.setText(classes[maxPos]);

// Releases model resources if no longer used.
   model.close();
  } catch(IOException e) {
// TODO Handle the exception
  }
  }

    // Method for performing logout actions
    private void performLogout() {
        // Implement your logout logic here
        // For example, clear session data, navigate to login screen, etc.

        // For demonstration purposes, let's assume you navigate to LoginActivity on logout
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Optional: close this activity upon logout
    }

    private int[] getTopNClassIndices(float[] confidences, int n) {
        int[] indices = new int[n];
        for (int i = 0; i < n; i++) {
            int maxIndex = -1;
            float maxConfidence = -1;
            for (int j = 0; j < confidences.length; j++) {
                if (confidences[j] > maxConfidence) {
                    maxConfidence = confidences[j];
                    maxIndex = j;
                }
            }
            indices[i] = maxIndex;
            confidences[maxIndex] = -1; // Set the confidence to a very low value to avoid selecting it again.
        }
        return indices;
    }

 @Override
protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
 if(resultCode == RESULT_OK){
     if(requestCode == 3){
       Bitmap image = (Bitmap) data.getExtras().get("data"); //get image as bitmap
       int dimension = Math.min(image.getWidth(), image.getHeight());
     image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
   imageView.setImageBitmap(image);


           image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
      classifyImage(image); //gets image and resize to imageSize

// next is for the camera
    }else{
     Uri dat = data.getData();
     Bitmap image = null;
     try {
  image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), dat);
} catch (IOException e) {
  e.printStackTrace();
}
   imageView.setImageBitmap(image);

   image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
     classifyImage(image);
}
}
   super.onActivityResult(requestCode, resultCode, data);
  }
}
