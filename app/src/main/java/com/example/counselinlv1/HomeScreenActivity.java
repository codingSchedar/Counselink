package com.example.counselinlv1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.counselinlv1.Adapters.PostAdapter;
import com.example.counselinlv1.Adapters.ReferralAdapter;
import com.example.counselinlv1.Adapters.AppointmentAdapter;
import com.example.counselinlv1.Models.Post;
import com.example.counselinlv1.Models.Referral;
import com.example.counselinlv1.Models.Appointment;
import com.example.counselinlv1.Utilities.ReferralDetailsDialog;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class HomeScreenActivity extends AppCompatActivity implements ReferralAdapter.OnReferralClickListener{

    private DrawerLayout drawerLayout;
    private TextView collegeNameText, userRoleText, emailText, addressText, phoneText;
    private RecyclerView recyclerView;
    private BottomNavigationView bottomNavigationView;
    private ImageView dropdownArrow, profileIcon, profileImage;
    private Button addButton;
    private List<Post> postList;
    private List<Referral> referralList;
    private List<Appointment> appointmentList;
    private PostAdapter postAdapter;
    private ReferralAdapter referralAdapter;
    private AppointmentAdapter appointmentAdapter;
    private FirebaseFirestore firestore;
    private String currentUserId, userType, departmentID;
    private NavigationView navigationView;
    private String currentView = "posts";
    private ListenerRegistration postsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        // Initialize Firestore instance
        firestore = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        currentUserId = intent.getStringExtra("userID");
        userType = intent.getStringExtra("userType"); // Get user type
        // Query to fetch the departmentID dynamically
        fetchUserDepartment();


        // Initialize Firebase references
        recyclerView = findViewById(R.id.home_screen_recyclerview);
        bottomNavigationView = findViewById(R.id.bottom_nav);
        dropdownArrow = findViewById(R.id.dropdown_arrow);
        profileIcon = findViewById(R.id.profile_picture);
        addButton = findViewById(R.id.add_button);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        postList = new ArrayList<>();
        referralList = new ArrayList<>();
        appointmentList = new ArrayList<>();
        postAdapter = new PostAdapter(postList, this);
        referralAdapter = new ReferralAdapter(referralList, this, this);
        appointmentAdapter = new AppointmentAdapter(appointmentList, this);
        recyclerView.setAdapter(postAdapter);

        // Load user data
        loadUserData();
        loadPosts();
        loadReferrals(userType, currentUserId,departmentID);

        // Set up listeners
        setupDropdownClickListener();
        setupAddButtonClickListener();
        setupBottomNavigation();
        setupProfileDrawerListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
        loadPosts(); // Reload posts when returning to the activity
    }


    @Override
    public void onReferralClick(Referral referral) {
        ReferralDetailsDialog dialog = new ReferralDetailsDialog(this, referral, userType, currentUserId, departmentID, newStatus -> {
            // Refresh RecyclerView after the status is updated
            loadReferrals(userType, currentUserId, departmentID);
        });
        dialog.show();
    }

    private ListenerRegistration departmentListener;

    private void fetchUserDepartment() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Remove any existing listener to avoid duplicate updates
        if (departmentListener != null) {
            departmentListener.remove();
        }

        // Listen for changes in the user's department
        departmentListener = firestore.collection("Users")
                .document(currentUserId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Failed to listen for department changes.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        String newDepartmentID = snapshot.getString("departmentID");

                        if (newDepartmentID != null && !newDepartmentID.equals(this.departmentID)) {
                            // Update the global departmentID and reload data
                            this.departmentID = newDepartmentID;
                            loadPosts();
                        }
                    } else {
                        Toast.makeText(this, "User data not found.", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void loadUserData() {
        firestore.collection("Users").document(currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Get user data from Firestore
                            userType = document.getString("type");
                            departmentID = document.getString("departmentID");
                            String firstName = document.getString("firstName");
                            String lastName = document.getString("lastName");
                            String email = document.getString("email");
                            String phone = document.getString("phone");
                            String address = document.getString("address");
                            String profilePictureUrl = document.getString("profilePictureUrl");

                            // Set data to drawer views
                            collegeNameText.setText(getDepartmentName(departmentID));  // Dynamic data
                            userRoleText.setText(userType); // Assuming user type is stored in "type"
                            emailText.setText(email);
                            addressText.setText(address);
                            phoneText.setText(phone);

                            // Load profile image using Glide
                            if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
                                Glide.with(this).load(profilePictureUrl).into(profileImage);
                                Glide.with(this).load(profilePictureUrl).into(profileIcon);
                            } else {
                                // Set default image if profile picture URL is null
                                profileImage.setImageResource(R.drawable.ic_user_box);
                            }

                            // Load posts by default
                            loadPosts();
                        } else {
                            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String getDepartmentName(String departmentID) {
        switch (departmentID) {
            case "LIMA":
                return "College of Marine Engineering and Technology";
            case "CAMP":
                return "College of Allied Medical Professions";
            case "CBA":
                return "College of Business Administration";
            case "CCJE":
                return "College of Criminal Justice Education";
            case "COD":
                return "College of Dentistry";
            case "CCAS":
                return "College of Computing Arts and Sciences";
            case "CON":
                return "College of Nursing";
            case "CITHM":
                return "College of International Tourism and Hospitality Management";
            case "ETEEAP":
                return "Expanded Tertiary Education Equivalency and Accreditation Program";
            case "CTE":
                return "College of Technical Education";
            default:
                return "Unknown Department";
        }
    }

    private void setupAddButtonClickListener() {
        addButton.setOnClickListener(v -> {
            Intent intent = null;
            if ("posts".equals(currentView)) {
                intent = new Intent(HomeScreenActivity.this, CreatePostActivity.class);
            } else if ("referrals".equals(currentView)) {
                intent = new Intent(HomeScreenActivity.this, AddNewReferralActivity.class);
            } else if ("appointments".equals(currentView)) {
                intent = new Intent(HomeScreenActivity.this, SetNewAppointmentActivity.class);
            }

            if (intent != null) {
                // Pass the current user ID to the new activity
                intent.putExtra("userID", currentUserId);
                intent.putExtra("departmentID",departmentID);
                startActivity(intent);
            }
        });
    }


    private void setupDropdownClickListener() {
        dropdownArrow.setOnClickListener(v -> {
            String[] options = {"Posts", "Referrals"};

            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Select an Option")
                    .setItems(options, (dialog, which) -> {
                        switch (which) {
                            case 0:
                                currentView = "posts";
                                loadPosts();
                                break;
                            case 1:
                                currentView = "referrals";
                                loadReferrals(userType, currentUserId, departmentID);
                                break;
                            case 2:
                                currentView = "appointments";
                                loadAppointments();
                                break;
                        }
                    })
                    .show();
        });
    }

    private void loadPosts() {
        currentView = "posts";
        recyclerView.setAdapter(postAdapter);
        addButton.setVisibility("counselor".equals(userType) ? View.VISIBLE : View.GONE);

        firestore.collection("Posts")
                .whereEqualTo("departmentID", departmentID)
                .orderBy("timestamp", Query.Direction.DESCENDING) // Sort by timestamp in descending order
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        postList.clear(); // Clear the list before adding new items
                        task.getResult().forEach(postSnapshot -> {
                            Post post = postSnapshot.toObject(Post.class);
                            postList.add(post);
                        });
                        postAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(HomeScreenActivity.this, "Error fetching posts", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Remove the posts listener if it exists
        if (postsListener != null) {
            postsListener.remove();
        }

        // Remove the department listener if it exists
        if (departmentListener != null) {
            departmentListener.remove();
        }
    }



    private void loadReferrals(String userType, String userId, String departmentID) {
        currentView = "referrals";
        recyclerView.setAdapter(referralAdapter);
        addButton.setVisibility(View.VISIBLE);

        if (userType.equals("student") || userType.equals("faculty")) {
            // Fetch referrals where the user is referred
            firestore.collection("Referrals")
                    .whereEqualTo("studentID", userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            referralList.clear();
                            task.getResult().forEach(referralSnapshot -> {
                                Referral referral = referralSnapshot.toObject(Referral.class);
                                if (referral != null) {
                                    referral.setId(referralSnapshot.getId());
                                    referralList.add(referral);
                                }
                            });

                            // Fetch referrals where the user is the referrer
                            firestore.collection("Referrals")
                                    .whereEqualTo("referrerID", userId)
                                    .orderBy("updatedAt", Query.Direction.DESCENDING)
                                    .get()
                                    .addOnCompleteListener(referrerTask -> {
                                        if (referrerTask.isSuccessful()) {
                                            referrerTask.getResult().forEach(referralSnapshot -> {
                                                Referral referral = referralSnapshot.toObject(Referral.class);
                                                if (referral != null) {
                                                    referral.setId(referralSnapshot.getId());
                                                    referralList.add(referral);
                                                }
                                            });

                                            referralAdapter.notifyDataSetChanged();
                                        } else {

                                        }
                                    });
                        } else {
                            Toast.makeText(HomeScreenActivity.this, "Error fetching referred data", Toast.LENGTH_SHORT).show();
                        }
                    });

        } else if (userType.equals("counselor")) {
            // Fetch all referrals
            firestore.collection("Referrals")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            referralList.clear();
                            task.getResult().forEach(referralSnapshot -> {
                                Referral referral = referralSnapshot.toObject(Referral.class);
                                if (referral != null) {
                                    referral.setId(referralSnapshot.getId());
                                    // Fetch the referred student's departmentID
                                    fetchStudentDepartment(referral, departmentID);
                                }
                            });
                        } else {
                            Toast.makeText(HomeScreenActivity.this, "Error fetching referrals for counselor", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(HomeScreenActivity.this, "Invalid user type", Toast.LENGTH_SHORT).show();
        }
    }

    // Helper method to fetch the referred student's departmentID
    private void fetchStudentDepartment(Referral referral, String counselorDepartmentID) {
        firestore.collection("Users")
                .document(referral.getStudentID())
                .get()
                .addOnCompleteListener(userTask -> {
                    if (userTask.isSuccessful() && userTask.getResult() != null) {
                        DocumentSnapshot userSnapshot = userTask.getResult();
                        String referredDepartmentID = userSnapshot.getString("departmentID");

                        if (referredDepartmentID != null && referredDepartmentID.equals(counselorDepartmentID)) {
                            referralList.add(referral);
                            referralAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Toast.makeText(HomeScreenActivity.this, "Error fetching student data", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void loadAppointments() {
        currentView = "appointments";
        recyclerView.setAdapter(appointmentAdapter);
        addButton.setVisibility(View.VISIBLE);

        firestore.collection("Appointments")
                .whereEqualTo("departmentID", departmentID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        appointmentList.clear();
                        task.getResult().forEach(appointmentSnapshot -> {
                            Appointment appointment = appointmentSnapshot.toObject(Appointment.class);
                            appointmentList.add(appointment);
                        });
                        appointmentAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(HomeScreenActivity.this, "Error fetching appointments", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupBottomNavigation() {
        // Hide the Analytics menu item if the user is not a counselor
        if (!"counselor".equals(userType)) {
            bottomNavigationView.getMenu().findItem(R.id.analytics).setVisible(false);
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.home) {
                loadPosts();
            } else if (id == R.id.notifications) {

                Intent notifIntent = new Intent(HomeScreenActivity.this, NotificationsActivity.class);
                // Pass current user ID and department ID as extras
                notifIntent.putExtra("userID", currentUserId);
                notifIntent.putExtra("departmentID", departmentID);
                startActivity(notifIntent);

            } else if (id == R.id.history) {
                startActivity(new Intent(HomeScreenActivity.this, HistoryActivity.class));
            } else if (id == R.id.analytics) {
                // Allow access only for counselors
                if ("counselor".equals(userType)) {
                    Intent analyticsIntent = new Intent(HomeScreenActivity.this, AnalyticsActivity.class);
                    // Pass current user ID and department ID as extras
                    analyticsIntent.putExtra("userID", currentUserId);
                    analyticsIntent.putExtra("departmentID", departmentID);
                    startActivity(analyticsIntent);
                } else {
                    Toast.makeText(this, "Access denied. Analytics is available for counselors only.", Toast.LENGTH_SHORT).show();
                }
            }
            return true;
        });

    }


    private void setupProfileDrawerListener() {
        profileIcon.setOnClickListener(v -> {
            drawerLayout.openDrawer(navigationView);
        });

        // Access the navigation view's header only after it's fully inflated
        View headerView = navigationView.getHeaderView(0);

        // Initialize the views inside the navigation drawer header
        collegeNameText = headerView.findViewById(R.id.college_name);
        userRoleText = headerView.findViewById(R.id.user_role);
        emailText = headerView.findViewById(R.id.email_text);
        addressText = headerView.findViewById(R.id.address_text);
        phoneText = headerView.findViewById(R.id.phone_text);
        profileImage = headerView.findViewById(R.id.profile_image);

        // Call loadUserData only after the header views are available
        loadUserData();

        // Handle the menu item clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.edit_profile) {
                // Handle the "Edit Profile" action
                Intent intent = new Intent(HomeScreenActivity.this, EditProfileActivity.class);
                intent.putExtra("userID", currentUserId);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_logout) {
                // Handle the "Log Out" action
                logout();
                return true;
            }

            return false;
        });

        // Handle close button click inside the drawer
        View closeDrawer = navigationView.getHeaderView(0).findViewById(R.id.close_drawer);
        closeDrawer.setOnClickListener(v -> {
            drawerLayout.closeDrawer(navigationView);
        });
    }

    private void logout() {
        // Clear session and navigate to LoginActivity
        startActivity(new Intent(HomeScreenActivity.this, LoginActivity.class));
        finish();
    }
}
