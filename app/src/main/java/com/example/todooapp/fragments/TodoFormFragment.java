package com.example.todooapp.fragments;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.todooapp.R;
import com.example.todooapp.data.model.Todo;
import com.example.todooapp.utils.ReminderHelper;
import com.example.todooapp.utils.TextFormattingManager;
import com.example.todooapp.utils.TodooDialogBuilder;
import com.example.todooapp.utils.UndoRedoManager;
import com.example.todooapp.viewmodel.TodoViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

public class TodoFormFragment extends Fragment {
    private EditText etTitle, etContent;
    private TodoViewModel todoViewModel;
    private String todoId = null;
    private TextView tvDate, tvCount;
    private LinearLayout bottomActionBar;
    private LinearLayout defaultOptionsContainer;
    private LinearLayout textOptionsContainer;
    private TextView btnToggleTextOptions;
    private TextFormattingManager textFormattingManager;
    private UndoRedoManager undoRedoManager;

    private TextWatcher contentWatcher;

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
    }

    private void initializeViews(View view) {
        // Initialize managers
        etTitle = view.findViewById(R.id.etTitle);
        etContent = view.findViewById(R.id.etContent);
        tvDate = view.findViewById(R.id.tvDate);
        tvCount = view.findViewById(R.id.tvCount);

        // Then initialize managers
        textFormattingManager = new TextFormattingManager(requireContext());
        undoRedoManager = new UndoRedoManager(etContent);

        // Get references to buttons that need to be shown/hidden
        TextView btnShare = view.findViewById(R.id.btnShare);
        TextView btnBackground = view.findViewById(R.id.btnBackground);
        TextView btnMenu = view.findViewById(R.id.btnMenu);
        TextView btnRedo = view.findViewById(R.id.btnRedo);
        TextView btnUndo = view.findViewById(R.id.btnUndo);
        TextView btnSave = view.findViewById(R.id.btnSave);

        // Get reference to the bottom action bar and its components
        bottomActionBar = view.findViewById(R.id.bottomActionBar);
        defaultOptionsContainer = view.findViewById(R.id.defaultOptionsContainer);
        textOptionsContainer = view.findViewById(R.id.textOptionsContainer);
        btnToggleTextOptions = view.findViewById(R.id.btnToggleTextOptions);

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

        // Set up toggle functionality for text formatting options
        btnToggleTextOptions.setOnClickListener(v -> {
            if (textOptionsContainer.getVisibility() == View.VISIBLE) {
                // Switch back to default options
                textOptionsContainer.setVisibility(View.GONE);
                defaultOptionsContainer.setVisibility(View.VISIBLE);
                // Change icon from X back to T
                btnToggleTextOptions.setText("\u0054"); // T icon
            } else {
                // Switch to text formatting options
                defaultOptionsContainer.setVisibility(View.GONE);
                textOptionsContainer.setVisibility(View.VISIBLE);
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
                        // Save state for undo
                        undoRedoManager.saveState(beforeChange, beforeCursorPos);
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


        // Set up other action buttons
        TextView btnAddRecord = view.findViewById(R.id.btnAddRecord);
        TextView btnAddImage = view.findViewById(R.id.btnAddImage);
        TextView btnLocation = view.findViewById(R.id.btnLocation);
        TextView btnAddCheckbox = view.findViewById(R.id.btnAddCheckbox);

        btnAddRecord.setOnClickListener(v -> {
            // Implement record functionality
            Toast.makeText(requireContext(), "Record feature coming soon", Toast.LENGTH_SHORT).show();
        });

        btnAddImage.setOnClickListener(v -> {
            // Implement add image functionality
            Toast.makeText(requireContext(), "Add image feature coming soon", Toast.LENGTH_SHORT).show();
        });

        btnLocation.setOnClickListener(v -> {
            // Implement location functionality
            Toast.makeText(requireContext(), "Location feature coming soon", Toast.LENGTH_SHORT).show();
        });

        btnAddCheckbox.setOnClickListener(v -> {
            // Implement checkbox functionality
            Toast.makeText(requireContext(), "Checkbox feature coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    // Update applyFormatting to save state before formatting
    private void applyFormatting(String formatType) {
        Editable editable = etContent.getText();
        int start = etContent.getSelectionStart();
        int end = etContent.getSelectionEnd();

        // For bullet points, get the current line if no selection
        if (formatType.equals("Bullet") && start == end) {
            int[] lineExtents = textFormattingManager.getLineExtents(editable.toString(), start);
            start = lineExtents[0];
            end = lineExtents[1];
        }

        if (start < end || formatType.equals("Bullet")) {
            // Save state for undo
            String currentContent = editable.toString();
            int currentCursorPos = etContent.getSelectionStart();
            undoRedoManager.saveState(currentContent, currentCursorPos);

            // Apply formatting based on type
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
                case "Bullet":
                    textFormattingManager.toggleBullet(editable, start, end);
                    break;
            }
        } else {
            Toast.makeText(requireContext(), "Please select text to format", Toast.LENGTH_SHORT).show();
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
            Context wrapper = new ContextThemeWrapper(requireContext(), R.style.CustomMenuTheme);
            PopupMenu popupMenu = new PopupMenu(wrapper, v);
            popupMenu.inflate(R.menu.menu_todo_options);

            popupMenu.setOnDismissListener(menu -> menuBackgroundOverlay.setVisibility(View.GONE));
            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.action_delete) {
                    // Show confirmation dialog instead of direct deletion
                    new TodooDialogBuilder(requireContext())
                            .setTitle("Delete Item")
                            .setMessage("What would you like to do with this item?")
                            .setPositiveButton("Move to Trash", (dialog, which) -> {
                                long id = Long.parseLong(todoId);
                                todoViewModel.getTodoById(id).observe(getViewLifecycleOwner(), currentTodo -> {
                                    if (currentTodo != null) {
                                        todoViewModel.moveToTrash(currentTodo);
                                        Toast.makeText(requireContext(), "Item moved to trash", Toast.LENGTH_SHORT).show();
                                        Navigation.findNavController(requireView()).popBackStack();
                                    }
                                });
                            })
                            .setNegativeButton("Delete Permanently", (dialog, which) -> {
                                deleteTodo();
                            })
                            .show();
                    return true;
                } else if (itemId == R.id.action_move_to) {
                    showCategorySelection();
                    return true;
                } else if (itemId == R.id.action_hide) {
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
                    return true;
                }else if (itemId == R.id.action_reminder) {
                    // Show date/time picker for reminder
                    showReminderDialog();
                    return true;
                }
                return false;
            });

            popupMenu.show();
        });

        menuBackgroundOverlay.setOnClickListener(v -> menuBackgroundOverlay.setVisibility(View.GONE));
    }

    // Modify loadTodoIfEditing to display the creation date and initial character count
    private void loadTodoIfEditing() {
        if (getArguments() != null && getArguments().getString("todoId") != null) {
            todoId = getArguments().getString("todoId");
            try {
                long id = Long.parseLong(todoId);
                todoViewModel.getTodoById(id).observe(getViewLifecycleOwner(), todo -> {
                    if (todo != null) {
                        etTitle.setText(todo.getTitle());
                        String content = todo.getContent();
                        etContent.setText(content);

                        undoRedoManager.clearHistory();

                        // Format and display creation date
                        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
                        String formattedDate = dateFormat.format(new Date(todo.getCreationDate()));
                        tvDate.setText("Created: " + formattedDate);

                        // Set initial character count
                        updateCharacterCount(content != null ? content.length() : 0);
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
        String content = etContent.getText().toString().trim();

        if (title.isEmpty() && !content.isEmpty()) {
            title = content.substring(0, Math.min(content.length(), 20)) + "...";
        }

        undoRedoManager.clearHistory();

        if (todoId == null) {
            Todo todo = new Todo();
            todo.setTitle(title);
            todo.setContent(content);
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
                        existingTodo.setContent(content);
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

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new androidx.activity.OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (etContent.hasFocus()) {
                            etContent.clearFocus();
                            requireView().findViewById(R.id.appBarLayout).requestFocus();
                            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager)
                                    requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(etContent.getWindowToken(), 0);
                        }
                    }
                });

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new androidx.activity.OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        String title = etTitle.getText().toString().trim();
                        String content = etContent.getText().toString().trim();
                        if (!title.isEmpty() || !content.isEmpty()) {
                            autoSaveTodo();
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

    private void showReminderDialog() {
        // Create a calendar instance with current date/time
        final Calendar calendar = Calendar.getInstance();

        // Create DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    // Set date to calendar
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    // After date is selected, show time picker
                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                            requireContext(),
                            (view1, hourOfDay, minute) -> {
                                // Set time to calendar
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);
                                calendar.set(Calendar.SECOND, 0);

                                // Save reminder time to todo
                                setReminder(calendar.getTimeInMillis());
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                    );
                    timePickerDialog.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Set minimum date to today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void setReminder(long reminderTime) {
        // Make sure we have a valid todoId
        if (todoId == null) {
            // Save the todo first and then set the reminder
            String title = etTitle.getText().toString().trim();
            String content = etContent.getText().toString().trim();

            if (title.isEmpty() && !content.isEmpty()) {
                title = content.substring(0, Math.min(content.length(), 20)) + "...";
            }

            Todo newTodo = new Todo();
            newTodo.setTitle(title);
            newTodo.setContent(content);
            newTodo.setTimestamp(System.currentTimeMillis());
            newTodo.setCreationDate(System.currentTimeMillis());
            newTodo.setHasReminder(true);
            newTodo.setReminderTime(reminderTime);

            todoViewModel.insert(newTodo);
            Toast.makeText(requireContext(), "Todo saved with reminder", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(requireView()).popBackStack();
            return;
        }

        try {
            long id = Long.parseLong(todoId);
            // Use a one-time observer pattern to avoid multiple toast messages
            final boolean[] handled = {false};
            todoViewModel.getTodoById(id).observe(getViewLifecycleOwner(), new androidx.lifecycle.Observer<Todo>() {
                @Override
                public void onChanged(Todo existingTodo) {
                    if (existingTodo != null && !handled[0] &&
                            getViewLifecycleOwner().getLifecycle().getCurrentState()
                                    .isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)) {

                        handled[0] = true;  // Mark as handled to prevent multiple executions

                        existingTodo.setHasReminder(true);
                        existingTodo.setReminderTime(reminderTime);
                        todoViewModel.update(existingTodo);

                        // Schedule the reminder
                        ReminderHelper.scheduleReminder(requireContext(), existingTodo);

                        // Format date for user feedback
                        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault());
                        String formattedDate = sdf.format(new Date(reminderTime));
                        Toast.makeText(requireContext(), "Reminder set for " + formattedDate, Toast.LENGTH_LONG).show();

                        // Remove the observer after handling
                        todoViewModel.getTodoById(id).removeObserver(this);
                    }
                }
            });
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Error setting reminder", Toast.LENGTH_SHORT).show();
        }
    }
}