import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

class Recipe {
    private String name;
    private String ingredients;
    private String steps;
    private int cookingTime;
    private String category;

    public Recipe(String name, String ingredients, String steps, int cookingTime, String category) {
        this.name = name;
        this.ingredients = ingredients;
        this.steps = steps;
        this.cookingTime = cookingTime;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public String getIngredients() {
        return ingredients;
    }

    public String toFileString() {
        return name + "|" + ingredients + "|" + steps + "|" + cookingTime + "|" + category;
    }

    public static Recipe fromFileString(String fileString) {
        String[] parts = fileString.split("\\|");
        if (parts.length < 5) return null; // ✅ Prevent crashes from invalid lines
        return new Recipe(parts[0], parts[1], parts[2], Integer.parseInt(parts[3]), parts[4]);
    }

    public String toString() {
        return "Name: " + name + "\n" +
               "Cooking Time: " + cookingTime + " minutes\n" +
               "Category: " + category + "\n" +
               "Ingredients: " + ingredients + "\n" +
               "Steps: " + steps + "\n";
    }
}

public class RecipeManager extends Frame {
    private TextField nameField, cookingTimeField, searchField;
    private TextArea ingredientsArea, stepsArea, recipeDetailsArea;
    private Choice categoryChoice;
    private java.awt.List recipeList;
    private Map<String, Recipe> recipes = new HashMap<>();
    private final String FILE_NAME = "recipes.txt";

    public RecipeManager() {
        setTitle("Recipe Manager");
        setSize(800, 600);
        setLayout(new BorderLayout());

        // ✅ Input panel
        Panel inputPanel = new Panel(new GridLayout(7, 2)); // fixed grid size
        inputPanel.add(new Label("Recipe Name:"));
        nameField = new TextField();
        inputPanel.add(nameField);

        inputPanel.add(new Label("Cooking Time (mins):"));
        cookingTimeField = new TextField();
        inputPanel.add(cookingTimeField);

        inputPanel.add(new Label("Ingredients:"));
        ingredientsArea = new TextArea(2, 20);
        inputPanel.add(ingredientsArea);

        inputPanel.add(new Label("Steps:"));
        stepsArea = new TextArea(3, 20);
        inputPanel.add(stepsArea);

        inputPanel.add(new Label("Category:"));
        categoryChoice = new Choice();
        categoryChoice.add("Breakfast");
        categoryChoice.add("Lunch");
        categoryChoice.add("Dinner");
        categoryChoice.add("Snack");
        inputPanel.add(categoryChoice);

        Button addRecipeButton = new Button("Add Recipe");
        addRecipeButton.addActionListener(e -> addRecipe());
        inputPanel.add(addRecipeButton);

        add(inputPanel, BorderLayout.NORTH);

        // ✅ Recipe list
        recipeList = new java.awt.List();
        recipeList.addItemListener(e -> showRecipeDetails(recipeList.getSelectedItem()));
        add(recipeList, BorderLayout.WEST);

        // ✅ Recipe details display
        recipeDetailsArea = new TextArea();
        recipeDetailsArea.setEditable(false);
        add(recipeDetailsArea, BorderLayout.CENTER);

        // ✅ Search panel
        Panel searchPanel = new Panel(new BorderLayout());
        searchPanel.add(new Label("Search Recipe:"), BorderLayout.NORTH);
        searchField = new TextField();
        searchPanel.add(searchField, BorderLayout.CENTER);
        Button searchButton = new Button("Search");
        searchButton.addActionListener(e -> searchRecipe());
        searchPanel.add(searchButton, BorderLayout.SOUTH);
        add(searchPanel, BorderLayout.SOUTH);

        // ✅ Load saved recipes
        loadRecipes();

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                saveRecipes();
                System.exit(0);
            }
        });
    }

    private void addRecipe() {
        try {
            String name = nameField.getText().trim();
            String cookingTimeText = cookingTimeField.getText().trim();
            int cookingTime = Integer.parseInt(cookingTimeText);
            String ingredients = ingredientsArea.getText().trim();
            String steps = stepsArea.getText().trim();
            String category = categoryChoice.getSelectedItem();

            if (name.isEmpty() || ingredients.isEmpty() || steps.isEmpty()) {
                throw new IllegalArgumentException("All fields must be filled.");
            }

            if (recipes.containsKey(name)) { // ✅ Prevent duplicates
                showMessage("A recipe with this name already exists.");
                return;
            }

            Recipe recipe = new Recipe(name, ingredients, steps, cookingTime, category);
            recipes.put(name, recipe);
            recipeList.add(name);
            saveRecipes();
            showMessage("Recipe added successfully!");
        } catch (NumberFormatException e) {
            showMessage("Please enter a valid number for cooking time.");
        } catch (IllegalArgumentException e) {
            showMessage(e.getMessage());
        }
    }

    private void showRecipeDetails(String recipeName) {
        Recipe recipe = recipes.get(recipeName);
        if (recipe != null) {
            recipeDetailsArea.setText(recipe.toString());
        }
    }

    private void searchRecipe() {
        String query = searchField.getText().toLowerCase();
        recipeList.removeAll();

        if (query.isEmpty()) { // ✅ Show all recipes if search box empty
            for (String name : recipes.keySet()) {
                recipeList.add(name);
            }
            return;
        }

        for (String name : recipes.keySet()) {
            if (name.toLowerCase().contains(query)) {
                recipeList.add(name);
            }
        }

        if (recipeList.getItemCount() == 0) {
            showMessage("No recipes found for the given search query.");
        }
    }

    private void saveRecipes() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Recipe recipe : recipes.values()) {
                writer.write(recipe.toFileString() + "\n");
            }
        } catch (IOException e) {
            showMessage("Error saving recipes: " + e.getMessage());
        }
    }

    private void loadRecipes() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Recipe recipe = Recipe.fromFileString(line);
                if (recipe != null) {
                    recipes.put(recipe.getName(), recipe);
                    recipeList.add(recipe.getName());
                }
            }
        } catch (IOException e) {
            // file might not exist on first run — ignore
        }
    }

    private void showMessage(String message) {
        Dialog dialog = new Dialog(this, "Message", true);
        dialog.setLayout(new FlowLayout());
        dialog.add(new Label(message));
        Button okButton = new Button("OK");
        okButton.addActionListener(e -> dialog.setVisible(false));
        dialog.add(okButton);
        dialog.setSize(350, 120);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        new RecipeManager().setVisible(true);
    }
}
