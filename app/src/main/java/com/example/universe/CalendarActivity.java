package com.example.universe;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class CalendarActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    Button addEventButton;
    TextView eventDisplay;
    ImageView eventImageView;
    private Uri selectedImageUri;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private List<Event> eventsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calendar);

        // Initialize Firebase references
        databaseReference = FirebaseDatabase.getInstance().getReference("events");
        storageReference = FirebaseStorage.getInstance().getReference("event_images");

        // Initialize UI elements
        bottomNavigationView = findViewById(R.id.bn);
        addEventButton = findViewById(R.id.addEventButton);
        eventDisplay = findViewById(R.id.eventDisplay);
        eventImageView = findViewById(R.id.eventImageView);


        eventDisplay.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showEditDeleteDialog();  // Show the dialog for edit/delete
                return true;
            }
        });

        // Set up navigation
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.b_profile) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                finish();
                return true;
            } else if (item.getItemId() == R.id.b_home) {
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                finish();
                return true;
            }
            return false;
        });

        // Add event button
        addEventButton.setOnClickListener(v -> showAddEventDialog());

        // Retrieve events on startup
        retrieveEvents();
    }

    private void showAddEventDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_event, null);
        builder.setView(dialogView);

        EditText eventName = dialogView.findViewById(R.id.input_event_name);
        DatePicker eventDate = dialogView.findViewById(R.id.input_event_date);
        TimePicker eventTime = dialogView.findViewById(R.id.input_event_time);
        Button saveEvent = dialogView.findViewById(R.id.button_save_event);
        Button addImageButton = dialogView.findViewById(R.id.selectImageButton);
        ImageView dialogEventImageView = dialogView.findViewById(R.id.dialog_event_image);

        // Preview selected image
        if (selectedImageUri != null) {
            dialogEventImageView.setImageURI(selectedImageUri);
            dialogEventImageView.setVisibility(View.VISIBLE);
        }

        AlertDialog dialog = builder.create();
        dialog.show();

        addImageButton.setOnClickListener(v -> pickImage());

        saveEvent.setOnClickListener(v -> {
            String name = eventName.getText().toString();
            int day = eventDate.getDayOfMonth();
            int month = eventDate.getMonth();
            int year = eventDate.getYear();
            int hour = eventTime.getHour();
            int minute = eventTime.getMinute();
            String eventDateStr = String.format("%02d/%02d/%d at %02d:%02d", day, month + 1, year, hour, minute);

            if (!name.isEmpty()) {
                saveEventToFirebase(name, eventDateStr);
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Please enter an event name.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveEventToFirebase(String name, String date) {
        if (selectedImageUri != null) {
            StorageReference imageRef = storageReference.child(System.currentTimeMillis() + ".jpg");
            imageRef.putFile(selectedImageUri).addOnSuccessListener(taskSnapshot ->
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        saveEventDetails(name, date, imageUrl);
                    })
            ).addOnFailureListener(e ->
                    Toast.makeText(this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
        } else {
            saveEventDetails(name, date, null);
        }
    }

    private void saveEventDetails(String name, String date, String imageUrl) {
        String eventId = databaseReference.push().getKey();
        Event event = new Event(name, date, imageUrl);
        databaseReference.child(eventId).setValue(event).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Event added successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to add event", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void retrieveEvents() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                eventsList.clear();
                for (DataSnapshot eventSnapshot : snapshot.getChildren()) {
                    Event event = eventSnapshot.getValue(Event.class);
                    if (event != null) {
                        // Set the ID to the event
                        event.setId(eventSnapshot.getKey()); // Store the ID
                        eventsList.add(event);
                    }
                }
                updateEventDisplay();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CalendarActivity.this, "Failed to load events", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void updateEventDisplay() {
        StringBuilder eventsText = new StringBuilder();
        for (Event event : eventsList) {
            eventsText.append(event.getName()).append(" on ").append(event.getDate()).append("\n");
            // Display event image if necessary here.
        }
        eventDisplay.setText(eventsText.toString());
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }


    private void saveEventToCalendar(String name, int day, int month, int year, int hour, int minute, ImageView eventImageView) {
        String eventDate = String.format("%02d/%02d/%d at %02d:%02d", day, month + 1, year, hour, minute);

        // Upload the image (if exists) and save event details
        if (selectedImageUri != null) {
            StorageReference imageRef = storageReference.child(System.currentTimeMillis() + ".jpg");
            imageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        Event event = new Event(name, eventDate, imageUrl);

                        // Save the event details in Firebase Database with unique ID
                        String eventId = databaseReference.push().getKey();
                        databaseReference.child(eventId).setValue(event);
                    }))
                    .addOnFailureListener(e -> Toast.makeText(CalendarActivity.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            // Save event without image
            Event event = new Event(name, eventDate, null);
            String eventId = databaseReference.push().getKey();
            databaseReference.child(eventId).setValue(event);
        }
    }
    private void showEditDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose an action");

        String[] options = {"Edit Event", "Delete Event"};
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                showEditEventDialog();  // Edit event option selected
            } else if (which == 1) {
                showDeleteEventDialog();  // Delete event option selected
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showEditEventDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_event, null);
        builder.setView(dialogView);

        EditText eventName = dialogView.findViewById(R.id.input_event_name);
        DatePicker eventDate = dialogView.findViewById(R.id.input_event_date);
        TimePicker eventTime = dialogView.findViewById(R.id.input_event_time);
        Button saveEvent = dialogView.findViewById(R.id.button_save_event);

        // Pre-fill with the first event for editing (for demo purposes)
        if (!eventsList.isEmpty()) {
            Event event = eventsList.get(0);  // Get the first event from the list
            eventName.setText(event.getName());  // Set event name

            // Set date and time if the date string is in a specific format (e.g., "dd/MM/yyyy at HH:mm")
            String[] dateParts = event.getDate().split(" at ");
            if (dateParts.length == 2) {
                String[] date = dateParts[0].split("/");  // Format: "dd/MM/yyyy"
                String[] time = dateParts[1].split(":");  // Format: "HH:mm"

                // Set DatePicker values
                eventDate.updateDate(Integer.parseInt(date[2]), Integer.parseInt(date[1]) - 1, Integer.parseInt(date[0]));

                // Set TimePicker values
                eventTime.setHour(Integer.parseInt(time[0]));
                eventTime.setMinute(Integer.parseInt(time[1]));
            }
        }

        AlertDialog dialog = builder.create();
        dialog.show();

        saveEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = eventName.getText().toString();
                int day = eventDate.getDayOfMonth();
                int month = eventDate.getMonth();
                int year = eventDate.getYear();
                int hour = eventTime.getCurrentHour();
                int minute = eventTime.getCurrentMinute();

                // Update the event (for demo, update the first event)
                saveEventToCalendar(name, day, month, year, hour, minute, eventImageView);
                dialog.dismiss(); // Dismiss the dialog after saving the event
                dialog.dismiss();
            }
        });
    }


    private void showDeleteEventDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Event");
        builder.setMessage("Are you sure you want to delete this event?");

        builder.setPositiveButton("Yes", (dialog, which) -> {
            // Delete the first event in the list (for demo purposes)
            if (!eventsList.isEmpty()) {
                Event eventToDelete = eventsList.get(0); // Change this to select the correct event
                databaseReference.child(eventToDelete.getId()).removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        eventsList.remove(eventToDelete); // Remove from local list
                        updateEventDisplay(); // Update UI
                        eventImageView.setImageURI(null); // Clear the image from the ImageView
                        eventImageView.setVisibility(View.GONE); // Hide the ImageView if needed
                        Toast.makeText(CalendarActivity.this, "Event deleted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(CalendarActivity.this, "Failed to delete event", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        builder.setNegativeButton("No", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                eventImageView.setImageURI(selectedImageUri);
            }
        }
    }
}
