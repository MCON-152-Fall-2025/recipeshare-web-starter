package com.mcon152.recipeshare.web;

import com.mcon152.recipeshare.Recipe;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

// MVC-Controller. Returns JSON (View via Spring converters).
// NOTE (SRP violation in current demo): this controller also stores data (LIst<Recipe>),
// generates IDs, and performs update Logic. In a refactor, move those to a Service/Repository.
@RestController
@RequestMapping("/api/recipes")
public class RecipeController {
    private static final Logger logger = LoggerFactory.getLogger(RecipeController.class);

    private final List<Recipe> recipes = new ArrayList<>();

    private final AtomicLong counter = new AtomicLong();

    RecipeController() {
    }

    /**
     * Adds a new recipe to the list.
     *
     * @param recipe the recipe to add
     * @return the added recipe with its assigned ID
     */
    @PostMapping
    public Recipe addRecipe(@RequestBody Recipe recipe) {
        logger.info("Incoming request: POST /api/recipes");
        logger.debug("Add recipe request - title='{}'", recipe.getTitle());

        try {
            recipe.setId(counter.incrementAndGet());
            recipes.add(recipe);
            logger.info("Created recipe with id={}", recipe.getId());
            return recipe;
        } catch (Exception e) {
            logger.error("Error occurred while adding recipe: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Retrieves all recipes.
     *
     * @return a list of all recipes
     */
    @GetMapping
    public List<Recipe> getAllRecipes() {
        logger.info("Incoming request: GET /api/recipes");

        try {
            List<Recipe> all = recipes;
            logger.info("Returning {} recipes", all.size());
            return all;
        } catch (Exception e) {
            logger.error("Error occurred while getting all recies: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Retrieves a recipe by its ID.
     *
     * @param id the ID of the recipe to retrieve
     * @return the recipe with the specified ID, or null if not found
     */
    @GetMapping("/{id}")
    public Recipe getRecipeById(@PathVariable long id) {
        logger.info("Incoming request: GET /api/recipes/{}", id);

        try {
            for (Recipe recipe: recipes) {
                if (recipe.getId() == id) {
                    logger.info("Found recipe with id={}", id);
                    return recipe;
                }
            }

            logger.warn("Recipe with id={} not found", id);
            return null;
        } catch (Exception e) {
            logger.error("Error occurred while retriving recipe {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Deletes a recipe by its ID.
     *
     * @param id the ID of the recipe to delete
     * @return true if the recipe was deleted, false if not found
     */
    @DeleteMapping("/{id}")
    public boolean deleteRecipe(@PathVariable long id) {
        logger.info("Incoming request: DELETE /api/recipes/{}", id);

        try {
            for (int i = 0; i < recipes.size(); i++) {
                if (recipes.get(i).getId() == id) {
                    recipes.remove(i);
                    logger.info("Deleted recipe with id={}", id);
                    return true;
                }
            }
            logger.warn("Delete failed - recipe with id={} not found", id);
            return false;
        } catch (Exception e) {
            logger.error("Error occurred while deleted recipe {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Updates an existing recipe by its ID.
     *
     * @param id            the ID of the recipe to update
     * @param updatedRecipe the updated recipe data
     * @return the updated recipe, or null if not found
     */
    @PutMapping("/{id}")
    public Recipe updateRecipe(@PathVariable long id, @RequestBody Recipe updatedRecipe) {
        MDC.put("recipeTitle", updatedRecipe.getTitle());
        logger.info("Incoming request: PUT /api/recipes/{}", id);
        logger.debug("Update recipe - id={}, newTitle='{}'", id, updatedRecipe.getTitle());

        try {
            for (int i = 0; i < recipes.size(); i++) {
                if (recipes.get(i).getId() == id) {
                    updatedRecipe.setId(id);
                    recipes.set(i, updatedRecipe);
                    logger.info("Updated recipe with id={}", id);
                    return updatedRecipe;
                }
            }

            logger.warn("Cannot update - recipe with id={} not found", id);
            return null;
        } catch (Exception e) {
            logger.error("Error occurred while updating recipe {}: {}", id, e.getMessage(), e);
            throw e;
        } finally {
            MDC.remove("recipeTitle");
        }
    }

    /**
     * Partially updates an existing recipe by its ID.
     *
     * @param id            the ID of the recipe to update
     * @param partialRecipe the partial recipe data to update
     * @return the updated recipe, or null if not found
     */
    @PatchMapping("/{id}")
    public Recipe patchRecipe(@PathVariable long id, @RequestBody Recipe partialRecipe) {
        MDC.put("recipeTitle", partialRecipe.getTitle());
        logger.info("Incoming request: PATCH /api/recipes/{}", id);
        logger.debug("Patch recipe - id={}, newTitle='{}'", id, partialRecipe.getTitle());

        try {
            for (int i = 0; i < recipes.size(); i++) {
                Recipe existing = recipes.get(i);
                if (existing.getId() == id) {
                    if (partialRecipe.getTitle() !=null) {
                        existing.setTitle(partialRecipe.getTitle());
                    }
                    if (partialRecipe.getDescription() != null) {
                        existing.setDescription(partialRecipe.getDescription());
                    }
                    if (partialRecipe.getIngredients() != null) {
                        existing.setIngredients(partialRecipe.getIngredients());
                    }
                    logger.info("Patched recipe with id={}", id);
                    return existing;
                }
            }

            logger.warn("Cannot patch - recipe with id={} not found", id);
            return null;
        } catch (Exception e) {
            logger.error("Error occurred while patching recipe {}: {}", id, e.getMessage(), e);
            throw e;
        } finally {
            MDC.remove("recipeTitle");
        }
    }
}