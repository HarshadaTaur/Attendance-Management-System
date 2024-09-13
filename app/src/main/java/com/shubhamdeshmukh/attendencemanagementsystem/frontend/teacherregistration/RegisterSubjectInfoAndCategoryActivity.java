package com.shubhamdeshmukh.attendencemanagementsystem.frontend.teacherregistration;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.shubhamdeshmukh.attendencemanagementsystem.R;
import com.shubhamdeshmukh.attendencemanagementsystem.backend.FirebaseDBConnection;
import com.shubhamdeshmukh.attendencemanagementsystem.backend.database_entities.Category;
import com.shubhamdeshmukh.attendencemanagementsystem.backend.database_entities.Class;
import com.shubhamdeshmukh.attendencemanagementsystem.backend.database_entities.Data;
import com.shubhamdeshmukh.attendencemanagementsystem.backend.database_entities.Subject;
import com.shubhamdeshmukh.attendencemanagementsystem.backend.models.BatchSelection;
import com.shubhamdeshmukh.attendencemanagementsystem.backend.models.ClassSelection;
import com.shubhamdeshmukh.attendencemanagementsystem.frontend.MainActivity;

import java.util.ArrayList;

public class RegisterSubjectInfoAndCategoryActivity extends AppCompatActivity {

    ArrayList<Category> categoryArrayList;

    int selectedSubjectIndex;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_subject_and_category);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button submitSubject = findViewById(R.id.SubmitSubject);


        submitSubject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                
            }
        });


        FloatingActionButton addCategoryFloatingButton = findViewById(R.id.addcategory_and_select_class);
        Intent intent = getIntent();
        selectedSubjectIndex = intent.getIntExtra("subjectFetchedDataIndex", -1);

        if (selectedSubjectIndex == -1) {

            categoryArrayList = MainActivity.dbConnection.getFetchedData().categories;
            
            Data registrationData = MainActivity.dbConnection.getRegistrationData();
            registrationData.subjects.add(new Subject());
            selectedSubjectIndex = registrationData.subjects.size() - 1;
        }
        else
        {
            Subject subject = MainActivity.dbConnection.getFetchedData().subjects.get(selectedSubjectIndex);
            EditText subjectName = findViewById(R.id.subject_name);
            EditText subjectCode = findViewById(R.id.subject_code);
            subjectName.setText(subject.getName());
            subjectCode.setText(subject.getCode());
            categoryArrayList = subject.getCategoryList();
        }
        RecyclerView recyclerView = findViewById(R.id.category_recyle_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new CategoryRegisterRecyclerAdapter(this, categoryArrayList, this));

        addCategoryFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    showCategoryInfoDialog(-1);
            }
        });
    }

    public void updateRecycler() {
        categoryArrayList = MainActivity.dbConnection.getRegistrationData().subjects.get(selectedSubjectIndex).getCategoryList();
        RecyclerView recyclerView = findViewById(R.id.category_recyle_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new CategoryRegisterRecyclerAdapter(this, categoryArrayList, this));

    }

    //    public ArrayList<Category> getCategoryArrayList() {
//        return categoryArrayList;
//    }

    public void showCategoryInfoDialog(int selectedCategoryIndex) {
        // Create an AlertDialog Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Inflate the custom layout
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_dialog_add_category, null);



        RecyclerView recyclerView = dialogView.findViewById(R.id.class_and_batch);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        FirebaseDBConnection.fetchData();
        ArrayList<Class> classList = MainActivity.dbConnection.getFetchedData().classes;

        ArrayList<ClassSelection> classSelectionArrayList = new ArrayList<>();
        for (Class _class:
             classList) {
            classSelectionArrayList.add(new ClassSelection(_class, false, new ArrayList<BatchSelection>()));
        }


        // Set the custom layout as the dialog's view
        builder.setView(dialogView);

        // Get the EditText and Button references
        EditText category_name = dialogView.findViewById(R.id.category_name);

        Button buttonSubmit = dialogView.findViewById(R.id.submit_category);

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        if (selectedCategoryIndex != -1)
        {
            category_name.setText(categoryArrayList.get(selectedCategoryIndex).getName());
            classList = categoryArrayList.get(selectedCategoryIndex).getClassList();
        }

        ClassSelectionRecyclerAdapter classSelectionRecyclerAdapter = new ClassSelectionRecyclerAdapter(getApplicationContext(), classSelectionArrayList);
        recyclerView.setAdapter(classSelectionRecyclerAdapter);

        // Set an OnClickListener for the Submit button
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle button click event

                ArrayList<ClassSelection> classSelectionArrayList1 = classSelectionRecyclerAdapter.getClassSelectionArrayList();
                
                ArrayList<Class> classArrayList = new ArrayList<>();

                for (ClassSelection classSelection:
                     classSelectionArrayList1) {
                    if (classSelection.isSelected())
                    {
                        Class _class = classSelection.getThisClass();
                        _class.setBatchList(new ArrayList<>());
                        ArrayList<BatchSelection> batchSelectionArrayList = classSelection.getBatchSelectionArrayList();
                        
                        for (BatchSelection batchSelection:
                             batchSelectionArrayList) {
                            if (batchSelection.isSelected())
                            {
                                _class.addBatch(batchSelection.getBatch());
                            }
                        }
                        
                        classArrayList.add(_class);
                    }
                }
                Log.d(MainActivity.TAG, "onClick: " + classArrayList);
                Data data = MainActivity.dbConnection.getRegistrationData();

                if (selectedCategoryIndex == -1)
                {
                    Category newCategory = new Category(category_name.getText().toString());
                    newCategory.setClassList(classArrayList);
                    data.subjects.get(selectedSubjectIndex).addCategory(newCategory);
                }
                else {
                    Category selectedCategory = data.subjects.get(selectedSubjectIndex).getCategoryList().get(selectedCategoryIndex);
                    selectedCategory.setName(category_name.getText().toString());
                    selectedCategory.setClassList(classArrayList);
                }

                MainActivity.dbConnection.setRegistrationData(data);
                MainActivity.dbConnection.completeRegistration();
                FirebaseDBConnection.updateDatabase();
//                Intent intent = new Intent(getApplicationContext(), RegisterAddClassesAndSubjectsActivity.class);
//
//                startActivity(intent);
//                finish();

                // Perform action with the input data
                // For example, you can validate inputs or submit them to a server

                // Dismiss the dialog after processing
                dialog.dismiss();
                updateRecycler();
            }
        });
    }


}