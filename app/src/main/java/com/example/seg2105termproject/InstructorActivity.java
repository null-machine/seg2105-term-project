package com.example.seg2105termproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
/**
 * This file is part of Course Booking application for Android devices
 *
 * app/src/main/java/com/example/seg2105termproject
 *
 * University of Ottawa - Faculty of Engineering - SEG2105 -Course Booking application for Android devices
 * @author      Sally R       <sraad062@uottawa.ca>
 *              Jerry S       <jsoon029@uottawa.ca>
 *              Glen W        <@uottawa.ca>
 *              Youssef J     <yjall032@uottawa.ca>
 *
*/
public class InstructorActivity extends AppCompatActivity {

    public static final String SELECTED_COURSE = "com.example.seg2105termproject.COURSE";

    static InstructorActivity self;
    TextView tvInstructorName, tvSelectedCourse, tvAssignedInstructor;
    EditText editCapacity, editDescription;
    Instructor instructor;
    Course course;

    RecyclerView instructorCoursesView, enrolledStudentsView;
    CoursesViewAdapter cAdapter;
    UsersViewAdapter uAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        self = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructor);

        tvInstructorName = findViewById(R.id.tvInstructorName);
        tvSelectedCourse = findViewById(R.id.tvSelectedCourse);
        tvAssignedInstructor = findViewById(R.id.tvAssignedInstructor);
        editCapacity = findViewById(R.id.editCapacity);
        editDescription = findViewById(R.id.editDescription);
        instructorCoursesView = findViewById(R.id.instructorCoursesView);
        enrolledStudentsView = findViewById(R.id.enrolledStudentsView);

        DatabaseHelper dbHelper = new DatabaseHelper(this);

        cAdapter = new CoursesViewAdapter(dbHelper.getAllCourses());
        LinearLayoutManager coursesLayoutManager = new LinearLayoutManager(this);
        instructorCoursesView.setLayoutManager(coursesLayoutManager);
        instructorCoursesView.setAdapter(cAdapter);

        uAdapter = new UsersViewAdapter(new User[0]);
        LinearLayoutManager usersLayoutManager = new LinearLayoutManager(this);
        enrolledStudentsView.setLayoutManager(usersLayoutManager);
        enrolledStudentsView.setAdapter(uAdapter);

        Intent intent = getIntent();
        instructor = (Instructor) dbHelper.getUser(intent.getStringExtra(MainActivity.EXTRA_USER));

        tvInstructorName.setText("Welcome " + instructor.getUsername());
        Log.d("sysout", "instructor loaded");
    }

    /**
     * Update the views on the activity.
     * @param course    The course selected by the instructor (user).
     */
    private void update(Course course) {
        this.course = course;
        tvSelectedCourse.setText(String.format("%s — %s", course.getCode(), course.getName()));

        editCapacity.setText(Integer.toString(course.getCapacity()));
        editDescription.setText(course.getDescription());
//        String desc = course.getDescription();
//        Log.d("sysout", "got here");
//        if (desc.equals("") || desc == null) tvDescription.setText("—");
//        else tvDescription.setText(desc);
//        Log.d("sysout", "crash");

        Instructor assignedInstructor = course.getInstructor();
        if (assignedInstructor != null) {
            Log.d("sysout", "course update, found instructor");
            tvAssignedInstructor.setText(assignedInstructor.getUsername());
        } else {
            Log.d("sysout", "course update, instructor not found");
            tvAssignedInstructor.setText(R.string.blank);
        }

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        cAdapter.refresh(dbHelper.getAllCourses());
        uAdapter.refresh(dbHelper.getEnrolledStudents(course.getId()));
    }

    /**
     * Method for the onClick of btnFindByName.
     * @param view  The view that calls this method.
     */
    public void findCourseByName(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Find Course By Name");
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        final EditText name = new EditText(this);
        name.setHint("Name");
        name.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        layout.addView(name);
        builder.setView(layout);
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String courseName = name.getText().toString();
                if (courseName.trim().equals("")) {
                    Utils.createErrorDialog(self, R.string.parameters_missing);
                    return;
                }
                DatabaseHelper dbHelper = new DatabaseHelper(self);
                try {
                    update(dbHelper.getCourseFromName(courseName));
                } catch (IllegalArgumentException e) {
                    Utils.createErrorDialog(self, R.string.course_not_found);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    /**
     * Method for the onClick of btnFindByCode.
     * @param view  The view that calls this method.
     */
    public void findCourseByCode(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Find Course By Code");
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        final EditText code = new EditText(this);
        code.setHint("Code");
        code.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        layout.addView(code);
        builder.setView(layout);
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String courseCode = code.getText().toString();
                if (courseCode.trim().equals("")) {
                    Utils.createErrorDialog(self, R.string.parameters_missing);
                    return;
                }
                DatabaseHelper dbHelper = new DatabaseHelper(self);
                try {
                    update(dbHelper.getCourse(courseCode));
                } catch (IllegalArgumentException e) {
                    Utils.createErrorDialog(self, R.string.course_not_found);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    /**
     * Method for the onClick of btnToggleAssign.
     * @param view  The view that calls this method.
     */
    public void toggleAssignInstructor(View view) {
        if (course == null) {
            Utils.createErrorDialog(this, R.string.no_course);
            return;
        }
        DatabaseHelper dbHelper = new DatabaseHelper(this);

        if (course.getInstructor() == null) {

            // Set the course instructor to the instructor using the app.
            dbHelper.changeCourseInstructor(course.getCode(), instructor);

            // To save on resources (and not call another database open), simply set the
            // selected course's instructor and update the views.
            course.setInstructor(instructor);
            update(course);

        } else if (course.getInstructor().equals(instructor)) {

            // Set the course instructor to null.
            dbHelper.changeCourseInstructor(course.getCode(), null);

            // To save on resources (and not call another database open), simply set the
            // selected course's instructor and update the views.
            course.setInstructor(null);
            update(course);

        } else {
            Utils.createErrorDialog(this, R.string.course_claimed);
        }
    }

    /**
     * Method for the onClick of btnUpdateCapDesc.
     * @param view  The view that calls this method.
     */
    public void updateCapDesc(View view) {
        if (course == null) {
            Utils.createErrorDialog(this, R.string.no_course);
            return;
        } else if (course.getInstructor() == null || !course.getInstructor().equals(instructor)){
            Utils.createErrorDialog(this, R.string.not_course_instructor);
            return;
        }

        DatabaseHelper dbHelper = new DatabaseHelper(this);

        Log.d("sysout", "update cd called");

        if (!editCapacity.getText().toString().equals("") && !editCapacity.getText().toString().equals("—")) {
            // Locked to number input, so long as setText does not add non-numeric characters.
            int newCapacity = Integer.parseInt(editCapacity.getText().toString());
            if (newCapacity > 0) {
                dbHelper.changeCourseCapacity(course.getCode(), newCapacity);
                course.setCapacity(newCapacity);
            } else {
                Utils.createErrorDialog(this, R.string.negative_capacity);
                editCapacity.setText(Integer.toString(course.getCapacity()));
            }
        } else {
            Utils.createErrorDialog(this, R.string.missing_fields);
        }


        String newDescription = editDescription.getText().toString();
        dbHelper.changeCourseDesc(course.getCode(), newDescription);
        course.setDescription(newDescription);
        update(course);
    }

    /**
     * Method for the onClick of btn.
     * @param view  The view that calls this method.
     */
    public void editCourseTimetable(View view){
        if (course == null) {
            Utils.createErrorDialog(this, R.string.no_course);
            return;
        } else if (course.getInstructor() == null || !course.getInstructor().equals(instructor)){
            Utils.createErrorDialog(this, R.string.not_course_instructor);
            return;
        }

        Intent intent = new Intent(this, TimetableActivity.class);
        intent.putExtra(SELECTED_COURSE, course.getCode());
        startActivity(intent);
    }
}