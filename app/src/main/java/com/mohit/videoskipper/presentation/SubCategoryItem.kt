package com.mohit.videoskipper.presentation

/**
 * A specific, granular item that the text-detection model matches against
 * (e.g. "Burger", "Pizza"). This is the actual keyword/phrase list used for matching.
 */
data class SubCategoryItem(
    val id: String,
    val label: String,
    val isBlocked: Boolean
)

/**
 * A top-level category (e.g. "Food") containing multiple SubCategoryItems.
 */
data class SkipCategory(
    val id: String,
    val label: String,
    val items: List<SubCategoryItem>
) {
    val blockedCount: Int get() = items.count { it.isBlocked }
}

/**
 * Starter data — expand these lists freely, or later load/persist via Room DB.
 * Each sub-item's `id` is what you'll match against detected OCR text/keywords.
 */
fun defaultCategories(): List<SkipCategory> = listOf(
    SkipCategory(
        id = "food",
        label = "Food",
        items = listOf(
            SubCategoryItem("burger", "Burger", false),
            SubCategoryItem("pizza", "Pizza", false),
            SubCategoryItem("vada_pav", "Vada Pav", false),
            SubCategoryItem("maggie", "Maggie", false),
            SubCategoryItem("biryani", "Biryani", false),
            SubCategoryItem("momos", "Momos", false),
            SubCategoryItem("street_food", "Street Food", false),
            SubCategoryItem("desserts", "Desserts", false)
        )
    ),
    SkipCategory(
        id = "music",
        label = "Music",
        items = listOf(
            SubCategoryItem("bollywood", "Bollywood", false),
            SubCategoryItem("hip_hop", "Hip-Hop", false),
            SubCategoryItem("edm", "EDM", false),
            SubCategoryItem("classical", "Classical", false),
            SubCategoryItem("lofi", "Lo-fi", false),
            SubCategoryItem("remix", "Remix", false)
        )
    ),
    SkipCategory(
        id = "sports",
        label = "Sports",
        items = listOf(
            SubCategoryItem("football", "Football", false),
            SubCategoryItem("cricket", "Cricket", false),
            SubCategoryItem("badminton", "Badminton", false),
            SubCategoryItem("kabaddi", "Kabaddi", false)
        )
    ),
    SkipCategory(
        id = "entertainment",
        label = "Entertainment",
        items = listOf(
            SubCategoryItem("comedy", "Comedy", false),
            SubCategoryItem("dance", "Dance", false),
            SubCategoryItem("gaming", "Gaming", false),
            SubCategoryItem("prank", "Prank", false)
        )
    )
)