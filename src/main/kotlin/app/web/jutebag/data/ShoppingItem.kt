package app.web.jutebag.data

data class ShoppingItem(
        val id: Long,
        val name: String,
        val qty: Long,
        val category: String, // grouped by category,
        val stored: Boolean = false)
