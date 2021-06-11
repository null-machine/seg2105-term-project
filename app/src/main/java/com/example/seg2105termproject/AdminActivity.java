package com.example.seg2105termproject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
//import android.widget.Button;

public class AdminActivity extends AppCompatActivity {

    // embarrassingly enough i don't know how to reference this class from within an alert so if someone could replace this singleton with that, that'd be great
    static AdminActivity self;

    Admin admin;

    //    Button btnCreateCourse, btnDeleteCourse, btnFindCName, btnFindCCode, btnDeleteUser;
    TextView tvAdminName;
    RecyclerView CoursesView, UsersView;

    CoursesViewAdapter cAdapter;
    UsersViewAdapter uAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        self = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        tvAdminName = findViewById(R.id.tvAdminName);

        CoursesView = findViewById(R.id.CoursesView);
        UsersView = findViewById(R.id.UsersView);

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        cAdapter = new CoursesViewAdapter(dbHelper.getAllCourses());
        uAdapter = new UsersViewAdapter(dbHelper.getAllUsers());

        LinearLayoutManager coursesLayoutManager = new LinearLayoutManager(this);
        LinearLayoutManager usersLayoutManager = new LinearLayoutManager(this);

        CoursesView.setLayoutManager(coursesLayoutManager);
        UsersView.setLayoutManager(usersLayoutManager);

        CoursesView.setAdapter(cAdapter);
        UsersView.setAdapter(uAdapter);

        Intent intent = getIntent();
        admin = (Admin) dbHelper.getUser(intent.getStringExtra(MainActivity.EXTRA_USER));

        tvAdminName.setText("Welcome " + admin.getUsername());
    }

    /**
     * Method for the onClick of btnCreateCourse.
     * @param view  The view that calls the method.
     */
    public void createCourse(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create Course");
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        final EditText name = new EditText(this);
        name.setHint("Name");
        layout.addView(name);
        final EditText code = new EditText(this);
        code.setHint("Code");
        layout.addView(code);
        builder.setView(layout);
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String courseName = name.getText().toString();
                String courseCode = code.getText().toString();
                DatabaseHelper dbHelper = new DatabaseHelper(self);

                // Needs to check for blank arguments (does not at the moment).
                try {
                    dbHelper.addCourse(new Course(courseName, courseCode));
                    Log.d("sysout", "course added: " + courseName + " " + courseCode);
                } catch (IllegalArgumentException e) {
                    Utils.createErrorDialog(self, R.string.course_already_exists);
                }

                self.refreshViews();
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
     * Method for the onClick of btnEditCCode.
     * @param view  The view that calls this method.
     */
    public void editCourseCode(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Course Code");
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        final EditText name = new EditText(this);
        name.setHint("Name");
        layout.addView(name);
        final EditText code = new EditText(this);
        code.setHint("New Code");
        layout.addView(code);
        builder.setView(layout);
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String courseName = name.getText().toString();
                String courseCode = code.getText().toString();
                DatabaseHelper dbHelper = new DatabaseHelper(self);
                try {
                    Course course = dbHelper.getCourseFromName(courseName);
                    if (course == null) {
                        Utils.createErrorDialog(self, R.string.course_not_found);
                        return;
                    }
                    String oldCode = course.getCourseCode();
                    course.setCourseCode(courseCode);
                    Log.d("sysout", "attempting deletion");
                    dbHelper.deleteCourse(oldCode); // not sure if theres a way to update without deleting but if data's being lost, it's probably because of this
                    Log.d("sysout", "deletion successful");
                    dbHelper.addCourse(course);
                    Log.d("sysout", "course code edited: " + courseName + " " + courseCode);
                } catch (IllegalArgumentException e) {
                    Utils.createErrorDialog(self, R.string.course_not_found);
                }

                self.refreshViews();
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
     * Method for the onClick of btnEditCName.
     * @param view  The view that calls this method.
     */
    public void editCourseName(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Course Name");
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        final EditText name = new EditText(this);
        name.setHint("New Name");
        layout.addView(name);
        final EditText code = new EditText(this);
        code.setHint("Code");
        layout.addView(code);
        builder.setView(layout);
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String courseName = name.getText().toString();
                String courseCode = code.getText().toString();
                DatabaseHelper dbHelper = new DatabaseHelper(self);
                try {
                    Course course = dbHelper.getCourse(courseCode);
                    if (course == null) {
                        Utils.createErrorDialog(self, R.string.course_not_found);
                        return;
                    }
                    dbHelper.deleteCourse(courseCode); // not sure if theres a way to update without deleting but if data's being lost, it's probably because of this
                    course.setCourseName(courseName);
                    dbHelper.addCourse(course);
                } catch (IllegalArgumentException e) {
                    Utils.createErrorDialog(self, R.string.course_not_found);
                }

                self.refreshViews();
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
     * Method for the onClick of btnDeleteCourse.
     * @param view  The view that calls this method.
     */
    public void deleteCourse(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Course");
        final EditText code = new EditText(this);
        code.setHint("Code");
        builder.setView(code);
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String courseCode = code.getText().toString();
                DatabaseHelper dbHelper = new DatabaseHelper(self);
                try {
                    if (!dbHelper.deleteCourse(courseCode)) {
                        Utils.createErrorDialog(self, R.string.course_not_found);
                    }
                } catch (IllegalArgumentException e) {
                    Utils.createErrorDialog(self, R.string.course_not_found);
                }

                self.refreshViews();
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
     * Method for the onClick of btnDeleteUser.
     * @param view  The view that calls this method.
     */
    public void deleteUser(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete User");
        final EditText name = new EditText(this);
        name.setHint("Username");
        builder.setView(name);
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String username = name.getText().toString();
                DatabaseHelper dbHelper = new DatabaseHelper(self);

                // Should probably prevent deletion of admin types.
                try {
                    User user = dbHelper.getUser(username);
                    if (!dbHelper.deleteUser(username)) {
                        Utils.createErrorDialog(self, R.string.user_not_found);
                    }
                } catch (IllegalArgumentException e) {
                    Utils.createErrorDialog(self, R.string.course_not_found);
                }

                self.refreshViews();
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
     * Refreshes the RecyclerViews with the latest User and Course data.
     */
    private void refreshViews() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        cAdapter.refresh(dbHelper.getAllCourses());
        uAdapter.refresh(dbHelper.getAllUsers());
    }
}