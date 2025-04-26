package com.example.todooapp.fragments;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.todooapp.R;
import com.example.todooapp.data.model.Todo;
import com.example.todooapp.utils.ReminderHelper;
import com.example.todooapp.utils.TodooDialogBuilder;
import com.example.todooapp.viewmodel.TodoViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Stack;

public class TodoFormFragment extends Fragment {
    private EditText etTitle, etContent;
    private TodoViewModel todoViewModel;
    private String todoId = null;

    // Add these as class variables
    private TextView tvDate, tvCount;

    // Add these fields to your TodoFormFragment class
    private final Stack<String> undoStack = new Stack<>();
    private final Stack<String> redoStack = new Stack<>();
    private String lastSavedContent = "";
    private TextWatcher contentWatcher;
    private boolean isUndoOrRedoInProgress = false;

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
        etTitle = view.findViewById(R.id.etTitle);
        etContent = view.findViewById(R.id.etContent);
        tvDate = view.findViewById(R.id.tvDate);
        tvCount = view.findViewById(R.id.tvCount);

        // Get references to buttons that need to be shown/hidden
        TextView btnShare = view.findViewById(R.id.btnShare);
        TextView btnBackground = view.findViewById(R.id.btnBackground);
        TextView btnMenu = view.findViewById(R.id.btnMenu);
        TextView btnRedo = view.findViewById(R.id.btnRedo);
        TextView btnUndo = view.findViewById(R.id.btnUndo);
        TextView btnSave = view.findViewById(R.id.btnSave);

        // Initially hide the editing buttons
        btnRedo.setVisibility(View.GONE);
        btnUndo.setVisibility(View.GONE);
        btnSave.setVisibility(View.GONE);

        etContent.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // Hide these buttons when editing content
                btnShare.setVisibility(View.GONE);
                btnBackground.setVisibility(View.GONE);
                btnMenu.setVisibility(View.GONE);

                // Show editing buttons
                btnRedo.setVisibility(View.VISIBLE);
                btnUndo.setVisibility(View.VISIBLE);
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

        // Setup character count listener for etContent
        etContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateCharacterCount(s.length());
            }
        });
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

                        clearHistory();

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

        clearHistory();

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
        EditText etContent = requireView().findViewById(R.id.etContent);

        // Always hide buttons initially
        btnUndo.setVisibility(View.GONE);
        btnRedo.setVisibility(View.GONE);

        // Initialize with current content
        lastSavedContent = etContent.getText().toString();

        // Set up TextWatcher to track changes
        contentWatcher = new TextWatcher() {
            private String beforeChange;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (!isUndoOrRedoInProgress) {
                    beforeChange = s.toString();
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not needed
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!isUndoOrRedoInProgress) {
                    String currentContent = s.toString();
                    // Only save meaningful changes to avoid excessive history
                    if (!currentContent.equals(beforeChange)) {
                        undoStack.push(beforeChange);
                        // Clear redo stack when new changes are made
                        redoStack.clear();
                    }
                }
                updateUndoRedoButtons();
            }
        };

        etContent.addTextChangedListener(contentWatcher);

        // Set up button click listeners
        btnUndo.setOnClickListener(v -> performUndo());
        btnRedo.setOnClickListener(v -> performRedo());

        // Initial button state
        updateUndoRedoButtons();
    }

    private void performUndo() {
        if (undoStack.isEmpty()) return;

        EditText etContent = requireView().findViewById(R.id.etContent);
        String currentContent = etContent.getText().toString();

        // Save current state for redo
        redoStack.push(currentContent);

        // Get previous state
        String previousContent = undoStack.pop();

        // Update content without triggering TextWatcher
        isUndoOrRedoInProgress = true;
        etContent.setText(previousContent);
        etContent.setSelection(previousContent.length());
        isUndoOrRedoInProgress = false;

        updateUndoRedoButtons();
    }

    private void performRedo() {
        if (redoStack.isEmpty()) return;

        EditText etContent = requireView().findViewById(R.id.etContent);
        String currentContent = etContent.getText().toString();

        // Save current state for undo
        undoStack.push(currentContent);

        // Get next state
        String nextContent = redoStack.pop();

        // Update content without triggering TextWatcher
        isUndoOrRedoInProgress = true;
        etContent.setText(nextContent);
        etContent.setSelection(nextContent.length());
        isUndoOrRedoInProgress = false;

        updateUndoRedoButtons();
    }

    private void updateUndoRedoButtons() {
        TextView btnUndo = requireView().findViewById(R.id.btnUndo);
        TextView btnRedo = requireView().findViewById(R.id.btnRedo);

        btnUndo.setEnabled(!undoStack.isEmpty());
        btnRedo.setEnabled(!redoStack.isEmpty());

        // Visual feedback for enabled/disabled state
        btnUndo.setAlpha(undoStack.isEmpty() ? 0.5f : 1.0f);
        btnRedo.setAlpha(redoStack.isEmpty() ? 0.5f : 1.0f);
    }

    // Call this method in onViewCreated after initializing your views
    private void clearHistory() {
        undoStack.clear();
        redoStack.clear();
        lastSavedContent = etContent.getText().toString();
        updateUndoRedoButtons();
    }

    // Make sure to remove the TextWatcher in onDestroyView
    @Override
    public void onDestroyView() {
        if (contentWatcher != null && etContent != null) {
            etContent.removeTextChangedListener(contentWatcher);
        }
        super.onDestroyView();
    }

    public String getLastSavedContent() {
        return lastSavedContent;
    }

    public void setLastSavedContent(String lastSavedContent) {
        this.lastSavedContent = lastSavedContent;
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