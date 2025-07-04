package com.example.todooapp.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.todooapp.R;
import com.example.todooapp.data.model.Todo;
import com.example.todooapp.utils.settings.SettingsManager;
import com.example.todooapp.utils.todoForm.HtmlConverter;
import com.example.todooapp.utils.todoForm.location.LocationHelper;
import com.example.todooapp.utils.todoForm.MediaHelper;
import com.example.todooapp.utils.todoForm.audio.RecordingHelper;
import com.example.todooapp.utils.todoForm.reminder.ReminderManager;
import com.example.todooapp.utils.todoForm.content.TextFormattingManager;
import com.example.todooapp.utils.todoForm.theme.ThemeHelper;
import com.example.todooapp.utils.shared.TodooDialogBuilder;
import com.example.todooapp.utils.todoForm.content.UndoRedoManager;
import com.example.todooapp.viewmodel.TodoViewModel;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TodoFormFragment extends Fragment {
    private EditText etTitle, etContent;
    private TodoViewModel todoViewModel;
    private String todoId = null;
    private TextView tvDate, tvCount;
    private LinearLayout bottomActionBar;
    private TextView btnToggleTextOptions;
    private TextFormattingManager textFormattingManager;
    private UndoRedoManager undoRedoManager;
    private static final int REQUEST_IMAGE_PICK = 100;

    private ReminderManager reminderManager;

    private TextWatcher contentWatcher;

    private RecordingHelper recordingHelper;
    private ThemeHelper themeHelper;
    private LocationHelper locationHelper;

    // Make sure this is in your class fields
    private SettingsManager settingsManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_todo_form, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        initializeViewModel();
        setupBackButton(view);
        setupMenuButton(view);
        setupUndoRedo();
        loadTodoIfEditing();

        // Add this line to make the checkboxes clickable
        etContent.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
    }

    private void initializeViews(View view) {
        // Initialize managers
        etTitle = view.findViewById(R.id.etTitle);
        etContent = view.findViewById(R.id.etContent);
        tvDate = view.findViewById(R.id.tvDate);
        tvCount = view.findViewById(R.id.tvCount);

        // Apply custom cursor color
        applyCursorColor(etTitle);
        applyCursorColor(etContent);

        // Then initialize managers
        textFormattingManager = new TextFormattingManager(requireContext());
        undoRedoManager = new UndoRedoManager(etContent);
        todoViewModel = new ViewModelProvider(requireActivity()).get(TodoViewModel.class);
        reminderManager = new ReminderManager(requireContext(), getViewLifecycleOwner(), todoViewModel);
        themeHelper = new ThemeHelper(this, todoViewModel);
        recordingHelper = new RecordingHelper(this, undoRedoManager, etContent);

        // Get references to buttons that need to be shown/hidden
        TextView btnShare = view.findViewById(R.id.btnShare);
        TextView btnBackground = view.findViewById(R.id.btnBackground);
        TextView btnMenu = view.findViewById(R.id.btnMenu);
        TextView btnRedo = view.findViewById(R.id.btnRedo);
        TextView btnUndo = view.findViewById(R.id.btnUndo);
        TextView btnSave = view.findViewById(R.id.btnSave);
        TextView btnStrikeThrough = view.findViewById(R.id.btnStrikeThrough);

        // Get reference to the bottom action bar and its components
        bottomActionBar = view.findViewById(R.id.bottomActionBar);
        LinearLayout defaultOptionsContainer = view.findViewById(R.id.defaultOptionsContainer);
        LinearLayout textOptionsContainer = view.findViewById(R.id.textOptionsContainer);
        btnToggleTextOptions = view.findViewById(R.id.btnToggleTextOptions);
        locationHelper = new LocationHelper(this, undoRedoManager, etContent);

        // Initially hide the editing buttons
        btnRedo.setVisibility(View.GONE);
        btnUndo.setVisibility(View.GONE);
        btnSave.setVisibility(View.GONE);

        // Set up focus listener on etContent
        etContent.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // Show bottom action bar when etContent is focused
                bottomActionBar.setVisibility(View.VISIBLE);

                // Hide these buttons when editing content
                btnShare.setVisibility(View.GONE);
                btnBackground.setVisibility(View.GONE);
                btnMenu.setVisibility(View.GONE);

                // Show editing buttons
                btnRedo.setVisibility(View.VISIBLE);
                btnUndo.setVisibility(View.VISIBLE);
                btnSave.setVisibility(View.VISIBLE);
            } else {
                // Hide bottom action bar when etContent loses focus
                bottomActionBar.setVisibility(View.GONE);

                // Restore original buttons when not editing
                btnShare.setVisibility(View.VISIBLE);
                btnBackground.setVisibility(View.VISIBLE);
                btnMenu.setVisibility(View.VISIBLE);

                // Hide editing buttons
                btnRedo.setVisibility(View.GONE);
                btnUndo.setVisibility(View.GONE);
                btnSave.setVisibility(View.GONE);
            }
        });

        btnToggleTextOptions.setOnClickListener(v -> {
            // Get references to the ScrollViews
            View textOptionsScrollView = requireView().findViewById(R.id.textOptionsScrollView);
            View defaultOptionsScrollView = requireView().findViewById(R.id.defaultOptionsScrollView);

            if (textOptionsScrollView.getVisibility() == View.VISIBLE) {
                // Switch back to default options
                textOptionsScrollView.setVisibility(View.GONE);
                defaultOptionsScrollView.setVisibility(View.VISIBLE);
                // Change icon from X back to T
                btnToggleTextOptions.setText("\u0054"); // T icon
            } else {
                // Switch to text formatting options
                defaultOptionsScrollView.setVisibility(View.GONE);
                textOptionsScrollView.setVisibility(View.VISIBLE);
                // Change icon from T to X
                btnToggleTextOptions.setText("\uf00d"); // X icon (f00d)
            }
        });

        // Set up click listeners for formatting buttons
        TextView btnBold = view.findViewById(R.id.btnBold);
        TextView btnHighlight = view.findViewById(R.id.btnHighlight);
        TextView btnItalic = view.findViewById(R.id.btnItalic);
        TextView btnUnderline = view.findViewById(R.id.btnUnderline);
        TextView btnBullet = view.findViewById(R.id.btnBullet);
        TextView btnH1 = view.findViewById(R.id.btnH1);

        // Add this in the initializeViews method after setting up the etContent listener
        etTitle.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // Hide these buttons when editing title
                btnShare.setVisibility(View.GONE);
                btnBackground.setVisibility(View.GONE);
                btnMenu.setVisibility(View.GONE);

                // Show only save button (redo and undo not needed for title)
                btnRedo.setVisibility(View.GONE);
                btnUndo.setVisibility(View.GONE);
                btnSave.setVisibility(View.VISIBLE);
            } else {
                // Restore original buttons when not editing
                btnShare.setVisibility(View.VISIBLE);
                btnBackground.setVisibility(View.VISIBLE);
                btnMenu.setVisibility(View.VISIBLE);

                // Hide editing buttons
                btnRedo.setVisibility(View.GONE);
                btnUndo.setVisibility(View.GONE);
                btnSave.setVisibility(View.GONE);
            }
        });

        // Setup save button click listener
        btnSave.setOnClickListener(v -> {
            autoSaveTodo();
            etContent.clearFocus();
            // Request focus on a different view to truly clear focus
            view.findViewById(R.id.appBarLayout).requestFocus();
        });

        btnShare.setOnClickListener(v -> {
            shareAsText();
        });

        btnBackground.setOnClickListener(v -> {
            themeHelper.showBackgroundOptions(todoId);
        });


        // In initializeViews() method
        contentWatcher = new TextWatcher() {
            private String beforeChange;
            private int beforeCursorPos;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (!undoRedoManager.isUndoOrRedoInProgress()) {
                    beforeChange = s.toString();
                    beforeCursorPos = etContent.getSelectionStart();
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not needed
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!undoRedoManager.isUndoOrRedoInProgress()) {
                    String currentContent = s.toString();
                    if (!currentContent.equals(beforeChange)) {
                        // Save state for undo - use current state not previous state
                        undoRedoManager.saveState(currentContent, etContent.getSelectionStart());
                        // Update the character count
                        updateCharacterCount(currentContent.length());
                    }
                }
            }
        };
        etContent.addTextChangedListener(contentWatcher);

        btnBold.setOnClickListener(v -> applyFormatting("bold"));
        btnItalic.setOnClickListener(v -> applyFormatting("italic"));
        btnUnderline.setOnClickListener(v -> applyFormatting("underline"));
        btnHighlight.setOnClickListener(v -> applyFormatting("highlight"));
        btnBullet.setOnClickListener(v -> applyFormatting("Bullet"));
        btnStrikeThrough.setOnClickListener(v -> applyFormatting("strikethrough"));
        btnH1.setOnClickListener(v -> applyFormatting("heading"));

        TextView btnAddRecord = view.findViewById(R.id.btnAddRecord);
        TextView btnAddImage = view.findViewById(R.id.btnAddImage);
        TextView btnLocation = view.findViewById(R.id.btnLocation);
        TextView btnAddCheckbox = view.findViewById(R.id.btnAddCheckbox);

        btnAddRecord.setOnClickListener(v -> {
            recordingHelper.checkPermissionAndRecord();
        });

        btnAddImage.setOnClickListener(v -> {
            selectImage();
        });

        btnLocation.setOnClickListener(v -> {
            locationHelper.checkPermissionAndGetLocation();
        });

        btnAddCheckbox.setOnClickListener(v -> {
            addCheckbox();
        });

        // In initializeViews method, after initializing managers
        settingsManager = new SettingsManager(requireContext());
        applyFontSize();
    }

    // Add this new method to apply font sizes
    private void applyFontSize() {
        String fontSize = settingsManager.getFontSize();
        float titleSize;
        float contentSize;

        switch (fontSize) {
            case "Small":
                titleSize = 20f;
                contentSize = 14f;
                break;
            case "Large":
                titleSize = 28f;
                contentSize = 18f;
                break;
            case "Medium":
            default:
                titleSize = 24f;
                contentSize = 16f;
                break;
        }

        etTitle.setTextSize(titleSize);
        etContent.setTextSize(contentSize);
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Image"), REQUEST_IMAGE_PICK);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                // Save image to local storage
                String localImagePath = MediaHelper.saveImageToLocal(requireContext(), selectedImageUri);

                if (localImagePath != null) {
                    // Get current cursor position
                    int cursorPosition = etContent.getSelectionStart();

                    // Save state for undo
                    undoRedoManager.saveFormatState();

                    // Insert image at cursor position
                    textFormattingManager.insertImage(etContent.getText(), cursorPosition, localImagePath);
                } else {
                    Toast.makeText(requireContext(), "Failed to save image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void applyFormatting(String formatType) {
        Editable editable = etContent.getText();
        int start = etContent.getSelectionStart();
        int end = etContent.getSelectionEnd();

        // Save state for undo BEFORE applying any formatting
        undoRedoManager.saveFormatState();

        if (start < end || formatType.equals("Bullet") || formatType.equals("heading")) {
            if (formatType.equals("Bullet")) {
                // Apply bullet formatting to all lines in the selected range
                textFormattingManager.toggleBullet(editable, start, end);
            } else if (formatType.equals("heading")) {
                // Apply heading formatting to all lines in the selected range
                textFormattingManager.toggleHeading(editable, start, end);
            } else {
                // Handle other formatting types
                switch (formatType) {
                    case "bold":
                        textFormattingManager.toggleBold(editable, start, end);
                        break;
                    case "italic":
                        textFormattingManager.toggleItalic(editable, start, end);
                        break;
                    case "underline":
                        textFormattingManager.toggleUnderline(editable, start, end);
                        break;
                    case "highlight":
                        textFormattingManager.toggleHighlight(editable, start, end);
                        break;
                    case "strikethrough":
                        textFormattingManager.toggleStrikeThrough(editable, start, end);
                        break;
                }
            }
        } else {
            // Single line handling (cursor position only)
            if (formatType.equals("Bullet")) {
                int[] lineExtents = textFormattingManager.getLineExtents(editable.toString(), start);
                textFormattingManager.toggleBullet(editable, lineExtents[0], lineExtents[1]);
            } else {
                Toast.makeText(requireContext(), "Please select text to format", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Update character count method
    private void updateCharacterCount(int count) {
        tvCount.setText(count + " characters");
    }

    private void initializeViewModel() {
        todoViewModel = new ViewModelProvider(requireActivity()).get(TodoViewModel.class);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupBackButton(View view) {
        TextView btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).start();
                    return false;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                    return false;
            }
            return false;
        });

        btnBack.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String content = etContent.getText().toString().trim();
            if (!title.isEmpty() || !content.isEmpty()) {
                autoSaveTodo();
            } else {
                Navigation.findNavController(view).popBackStack();
            }
        });
    }

    private void setupMenuButton(View view) {
        TextView btnMenu = view.findViewById(R.id.btnMenu);
        View menuBackgroundOverlay = view.findViewById(R.id.menuBackgroundOverlay);

        btnMenu.setOnClickListener(v -> {
            menuBackgroundOverlay.setVisibility(View.VISIBLE);

            // Create a custom view for our menu options
            View menuView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_menu_options, null);

            // Create the TodooDialogBuilder with consistent styling
            TodooDialogBuilder builder = new TodooDialogBuilder(requireContext());
            builder.setTitle("Menu Options")
                    .setView(menuView);

            // Get the menu options from the view
            TextView btnDelete = menuView.findViewById(R.id.menu_delete);
            TextView btnMoveTo = menuView.findViewById(R.id.menu_move_to);
            TextView btnHide = menuView.findViewById(R.id.menu_hide);
            TextView btnReminder = menuView.findViewById(R.id.menu_reminder);

            // Set text colors from dialog theme
            int textColor = builder.getTextColor();
            btnDelete.setTextColor(textColor);
            btnMoveTo.setTextColor(textColor);
            btnHide.setTextColor(textColor);
            btnReminder.setTextColor(textColor);

            // Set click listeners
            AlertDialog dialog = builder.show();

            btnDelete.setOnClickListener(item -> {
                dialog.dismiss();
                menuBackgroundOverlay.setVisibility(View.GONE);

                // Show confirmation dialog for deletion
                new TodooDialogBuilder(requireContext())
                        .setTitle("Delete Item")
                        .setMessage("What would you like to do with this item?")
                        .setPositiveButton("Move to Trash", (dialogInterface, which) -> {
                            long id = Long.parseLong(todoId);
                            todoViewModel.getTodoById(id).observe(getViewLifecycleOwner(), currentTodo -> {
                                if (currentTodo != null) {
                                    todoViewModel.moveToTrash(currentTodo);
                                    Toast.makeText(requireContext(), "Item moved to trash", Toast.LENGTH_SHORT).show();
                                    Navigation.findNavController(requireView()).popBackStack();
                                }
                            });
                        })
                        .setNegativeButton("Delete Permanently", (dialogInterface, which) -> {
                            deleteTodo();
                        })
                        .show();
            });

            btnMoveTo.setOnClickListener(item -> {
                dialog.dismiss();
                menuBackgroundOverlay.setVisibility(View.GONE);
                showCategorySelection();
            });

            btnHide.setOnClickListener(item -> {
                dialog.dismiss();
                menuBackgroundOverlay.setVisibility(View.GONE);

                long id = Long.parseLong(todoId);
                todoViewModel.getTodoById(id).observe(getViewLifecycleOwner(), currentTodo -> {
                    if (currentTodo != null) {
                        todoViewModel.hideItem(currentTodo);
                        Toast.makeText(
                                requireContext(),
                                "Item hidden. Pull down from the top to see hidden todos.",
                                Toast.LENGTH_LONG
                        ).show();
                        Navigation.findNavController(requireView()).popBackStack();
                    }
                });
            });

            btnReminder.setOnClickListener(item -> {
                dialog.dismiss();
                menuBackgroundOverlay.setVisibility(View.GONE);
                showReminderDialog();
            });

            dialog.setOnDismissListener(dialogInterface -> {
                menuBackgroundOverlay.setVisibility(View.GONE);
            });
        });

        menuBackgroundOverlay.setOnClickListener(v -> menuBackgroundOverlay.setVisibility(View.GONE));
    }

    private void loadTodoIfEditing() {
        if (getArguments() != null && getArguments().getString("todoId") != null) {
            todoId = getArguments().getString("todoId");
            try {
                long id = Long.parseLong(todoId);
                todoViewModel.getTodoById(id).observe(getViewLifecycleOwner(), todo -> {
                    if (todo != null) {
                        etTitle.setText(todo.getTitle());

                        // Convert HTML to formatted Spannable
                        String htmlContent = todo.getContent();
                        if (htmlContent != null && !htmlContent.isEmpty()) {
                            Spannable spannableContent = HtmlConverter.fromHtml(requireContext(), htmlContent);
                            etContent.setText(spannableContent);

                            // Set initial character count
                            updateCharacterCount(spannableContent.length());
                        }

                        undoRedoManager.clearHistory();

                        // Format and display creation date
                        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
                        String formattedDate = dateFormat.format(new Date(todo.getCreationDate()));
                        tvDate.setText("Created: " + formattedDate);

                        // Load and apply theme using ThemeHelper
                        themeHelper.setCurrentTheme(todo.getThemeIndex());
                        themeHelper.applyCurrentTheme();
                    }
                });
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Invalid todo ID", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).popBackStack();
            }
        } else {
            // For a new todo, show current date as creation date
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
            String formattedDate = dateFormat.format(new Date(System.currentTimeMillis()));
            tvDate.setText("Created: " + formattedDate);

            // Initialize with 0 characters
            updateCharacterCount(0);
        }
    }

    private void deleteTodo() {
        if (todoId != null) {
            try {
                long id = Long.parseLong(todoId);
                todoViewModel.getTodoById(id).observe(getViewLifecycleOwner(), todo -> {
                    if (todo != null) {
                        todoViewModel.delete(todo);
                        Toast.makeText(requireContext(), "Todo deleted", Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(requireView()).popBackStack();
                    }
                });
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Invalid todo ID", Toast.LENGTH_SHORT).show();
            }
        } else {
            Navigation.findNavController(requireView()).popBackStack();
        }
    }

    private void showCategorySelection() {
        if (todoId == null) {
            autoSaveTodo();
            Toast.makeText(requireContext(), "Please save your todo first", Toast.LENGTH_SHORT).show();
            return;
        }

        Bundle bundle = new Bundle();
        long[] todoIds = new long[1];
        todoIds[0] = Long.parseLong(todoId);
        bundle.putLongArray("selectedTodoIds", todoIds);
        Navigation.findNavController(requireView())
                .navigate(R.id.action_todoFormFragment_to_categoryManagementFragment, bundle);
    }

    private void autoSaveTodo() {
        String title = etTitle.getText().toString().trim();

        // Get content as Spannable to preserve formatting
        Spannable spannableContent = etContent.getText();
        // Convert to HTML string for storage
        String htmlContent = HtmlConverter.toHtml(requireContext(), spannableContent);

        if (title.isEmpty() && !htmlContent.isEmpty()) {
            // Strip HTML for title preview
            String plainContent = android.text.Html.fromHtml(htmlContent).toString();
            title = plainContent.substring(0, Math.min(plainContent.length(), 20)) + "...";
        }

        undoRedoManager.clearHistory();

        if (todoId == null) {
            Todo todo = new Todo();
            todo.setTitle(title);
            todo.setContent(htmlContent); // Store HTML content
            todo.setTimestamp(System.currentTimeMillis());
            todo.setCreationDate(System.currentTimeMillis());
            todoViewModel.insert(todo);
            Navigation.findNavController(requireView()).popBackStack();
        } else {
            try {
                long id = Long.parseLong(todoId);
                String finalTitle = title;
                todoViewModel.getTodoById(id).observe(getViewLifecycleOwner(), existingTodo -> {
                    if (existingTodo != null && getViewLifecycleOwner().getLifecycle().getCurrentState()
                            .isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)) {
                        existingTodo.setTitle(finalTitle);
                        existingTodo.setContent(htmlContent); // Store HTML content
                        existingTodo.setTimestamp(System.currentTimeMillis());
                        todoViewModel.update(existingTodo);
                        Navigation.findNavController(requireView()).popBackStack();
                    }
                });
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Invalid todo ID", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        applyFontSize();

        // Remove any existing callbacks first to avoid duplicates
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new androidx.activity.OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (etContent.hasFocus() || etTitle.hasFocus()) {
                            // First clear focus from edit fields
                            etContent.clearFocus();
                            etTitle.clearFocus();
                            // Request focus on a non-input view
                            requireView().findViewById(R.id.appBarLayout).requestFocus();
                            // Hide keyboard
                            android.view.inputmethod.InputMethodManager imm =
                                    (android.view.inputmethod.InputMethodManager)
                                            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(etContent.getWindowToken(), 0);

                            // Important: don't navigate back yet, just clear focus
                            return;
                        }

                        // If we're not handling focus clearing, save if needed and go back
                        String title = etTitle.getText().toString().trim();
                        String content = etContent.getText().toString().trim();
                        if (!title.isEmpty() || !content.isEmpty()) {
                            autoSaveTodo();
                        } else {
                            // No content to save, just go back
                            Navigation.findNavController(requireView()).popBackStack();
                        }
                    }
                });
    }

    private void setupUndoRedo() {
        TextView btnUndo = requireView().findViewById(R.id.btnUndo);
        TextView btnRedo = requireView().findViewById(R.id.btnRedo);

        btnUndo.setOnClickListener(v -> undoRedoManager.undo());
        btnRedo.setOnClickListener(v -> undoRedoManager.redo());

        undoRedoManager.setOnUndoRedoStateChangedListener((canUndo, canRedo) -> {
            btnUndo.setEnabled(canUndo);
            btnRedo.setEnabled(canRedo);
            btnUndo.setAlpha(canUndo ? 1.0f : 0.5f);
            btnRedo.setAlpha(canRedo ? 1.0f : 0.5f);
        });
    }

    // Make sure to remove the TextWatcher in onDestroyView
    @Override
    public void onDestroyView() {
        if (contentWatcher != null && etContent != null) {
            etContent.removeTextChangedListener(contentWatcher);
        }
        super.onDestroyView();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RecordingHelper.REQUEST_RECORD_AUDIO_PERMISSION &&
                grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            recordingHelper.showRecordingDialog();
        } else if (requestCode == LocationHelper.REQUEST_LOCATION_PERMISSION &&
                grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            locationHelper.getCurrentLocationAndInsert();
        } else if (requestCode == RecordingHelper.REQUEST_RECORD_AUDIO_PERMISSION) {
            Toast.makeText(requireContext(), "Recording permission is required",
                    Toast.LENGTH_SHORT).show();
        } else if (requestCode == LocationHelper.REQUEST_LOCATION_PERMISSION) {
            Toast.makeText(requireContext(), "Location permission is required",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void shareAsText() {
        String title = etTitle.getText().toString().trim();
        String content = android.text.Html.fromHtml(
                HtmlConverter.toHtml(requireContext(), etContent.getText()),
                android.text.Html.FROM_HTML_MODE_COMPACT).toString();

        String shareText = title.isEmpty() ? content : title + "\n\n" + content;

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, "Share Note");
        startActivity(shareIntent);
    }

    private void addCheckbox() {
        Editable editable = etContent.getText();
        int start = etContent.getSelectionStart();
        int end = etContent.getSelectionEnd();

        // Save state for undo before applying any checkbox
        undoRedoManager.saveFormatState();

        // Apply checkbox to the selected range (or current line if no selection)
        if (start == end) {
            // No selection: apply to the current line
            int[] lineExtents = textFormattingManager.getLineExtents(editable.toString(), start);
            textFormattingManager.toggleCheckbox(editable, lineExtents[0], lineExtents[1]);
        } else {
            // Selection: apply to all lines in the range
            textFormattingManager.toggleCheckbox(editable, start, end);
        }
    }

    // Replace showReminderDialog method with a call to the manager
    private void showReminderDialog() {
        reminderManager.showReminderDialog(todoId, newTodo -> {
            // Set title and content for the new todo
            String title = etTitle.getText().toString().trim();
            String content = etContent.getText().toString().trim();

            if (title.isEmpty() && !content.isEmpty()) {
                title = content.substring(0, Math.min(content.length(), 20)) + "...";
            }

            newTodo.setTitle(title);
            newTodo.setContent(content);

            // Insert the todo
            todoViewModel.insert(newTodo);
            Toast.makeText(requireContext(), "Todo saved with reminder", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(requireView()).popBackStack();
        });
    }

    private void applyCursorColor(EditText editText) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // For Android 10+ (API 29+)
            editText.setTextCursorDrawable(R.drawable.custom_cursor);
        } else {
            // For older Android versions using reflection
            try {
                Field f = TextView.class.getDeclaredField("mCursorDrawableRes");
                f.setAccessible(true);
                f.set(editText, R.drawable.custom_cursor);
            } catch (Exception ignored) { }
        }
    }
}