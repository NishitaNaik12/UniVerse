package com.example.universe;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class CalendarActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    Button addEventButton;  // Declare the button for adding events
    TextView eventDisplay;  // TextView to display events
    List<String> eventsList = new ArrayList<>();  // List to store added events

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calendar);

        bottomNavigationView = findViewById(R.id.bn);

        // Initialize the event display TextView
        eventDisplay = findViewById(R.id.eventDisplay);  // Initialize TextView for event display

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id=item.getItemId();
                if(id==R.id.b_profile)
                {
                    startActivity(new Intent(getApplicationContext(),ProfileActivity.class));
                    finish();
                    return  true;

                }
                else if(id == R.id.b_home){
                    startActivity(new Intent(getApplicationContext(),HomeActivity.class));
                    finish();
                    return  true;
                }
                else {
                    return true;
                }
            }
        });

        addEventButton = findViewById(R.id.addEventButton);
        addEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddEventDialog();  // Call the method to show the overlay dialog
            }
        });

        eventDisplay.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showEditDeleteDialog();  // Show the dialog for edit/delete
                return true;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        bottomNavigationView.setSelectedItemId(R.id.b_calendar);
    }

    private void showAddEventDialog() {
        // Inflate the custom layout
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_event, null);
        builder.setView(dialogView);

        // Get references to the dialog components
        EditText eventName = dialogView.findViewById(R.id.input_event_name);
        DatePicker eventDate = dialogView.findViewById(R.id.input_event_date);
        Button saveEvent = dialogView.findViewById(R.id.button_save_event);

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // Handle Save button click
        saveEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = eventName.getText().toString();
                int day = eventDate.getDayOfMonth();
                int month = eventDate.getMonth();
                int year = eventDate.getYear();

                // Call method to save the event
                saveEventToCalendar(name, day, month, year);
                dialog.dismiss();  // Dismiss the dialog after saving the event
            }
        });
    }

    private void addEventToList(String name, int day, int month, int year) {
        String eventDetails = "Event: " + name + " on " + day + "/" + (month + 1) + "/" + year;
        Log.d("Event", "Adding event:" + eventDetails);
        eventsList.add(eventDetails);  // Add event details to the list
        updateEventDisplay();  // Update the display
    }

    private void updateEventDisplay() {
        StringBuilder eventsText = new StringBuilder();
        Log.d("EventList", "Total events: " + eventsList.size());
        for (String event : eventsList) {
            eventsText.append(event).append("\n");
        }

        Log.d("EventDisplay", "Updating display with: " + eventsText.toString());
        eventDisplay.setText(eventsText.toString());  // Display all events in the TextView
    }

    // Method to handle saving the event
    private void saveEventToCalendar(String name, int day, int month, int year) {
        addEventToList(name, day, month, year);
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
        Button saveEvent = dialogView.findViewById(R.id.button_save_event);

        // Pre-fill with the first event for editing (for demo purposes)
        if (!eventsList.isEmpty()) {
            String[] eventParts = eventsList.get(0).split(" on ");
            eventName.setText(eventParts[0].replace("Event: ", ""));
            // Set the date in DatePicker manually (can enhance this for other events)
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

                // Update the event (for demo, update the first event)
                if (!eventsList.isEmpty()) {
                    eventsList.set(0, "Event: " + name + " on " + day + "/" + (month + 1) + "/" + year);
                    updateEventDisplay();
                }
                dialog.dismiss();
            }
        });
    }

    // Method to delete an event
    private void showDeleteEventDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Event");
        builder.setMessage("Are you sure you want to delete this event?");

        builder.setPositiveButton("Yes", (dialog, which) -> {
            // Delete the first event in the list (for demo purposes)
            if (!eventsList.isEmpty()) {
                eventsList.remove(0);  // You can update this to remove specific events
                updateEventDisplay();
            }
            Toast.makeText(CalendarActivity.this, "Event deleted", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("No", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}