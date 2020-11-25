package app.web.jutebag.data


data class SaveRequest(val email: String,     // shopping cart identifier - UNSAFE, replace
                       val items: List<ShoppingItem>,
                       val categories: List<Category>,
                       val revision: Int)
