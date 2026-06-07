# Laboratorio 7 - MVVM en Compose: App de Productos

## 1. Fundamentos Arquitectónicos (MVVM)

Para evitar que el código de la interfaz y la lógica de negocio se mezclen (lo cual genera aplicaciones difíciles de mantener y probar), se utiliza el patrón **MVVM (Model-View-ViewModel)**. Este es el estándar recomendado por Google para Jetpack Compose.

### Componentes de MVVM

*
**Model:** Representa los datos y las reglas de negocio (provenientes de APIs, bases de datos o repositorios).


* **View:** La interfaz de usuario (en Compose, formada por funciones `@Composable`). Su única función es mostrar datos, recibir eventos del usuario y delegar acciones al ViewModel. **No debe contener lógica de negocio compleja**.


* **ViewModel:** Actúa como intermediario. Almacena y gestiona el estado de la UI de forma que sobreviva a los cambios de configuración (como rotaciones de pantalla). Expone el estado usando `StateFlow`.



---

## 2. Arquitectura y Estructura del Proyecto

Para cumplir con el **Repository Pattern** y los principios **SOLID**, debes organizar tu código en los siguientes paquetes:

```text
app/src/main/java/com/tuusuario/productapp/
│
├── data/
│   ├── Product.kt                 // Data class del Producto
│   └── Category.kt                // Data class o Enum de Categorías
│
├── repository/
│   └── ProductRepository.kt       // Lógica de acceso a datos (Simulado en memoria)
│
├── viewmodel/
│   ├── ProductViewModel.kt        // Gestión de estados y eventos
│   └── ProductViewModelFactory.kt // Fábrica para instanciar el ViewModel con dependencias
│
└── ui/
    ├── ProductScreen.kt           // Pantalla principal (Lista y búsquedas)
    ├── ProductFormScreen.kt       // Formulario para Crear / Editar
    ├── ProductDetailScreen.kt     // Detalle del producto
    └── ProductItem.kt             // Card reutilizable para cada producto

```

---

## 3. Implementación de la Base Técnica

### 3.1. Capa de Modelo (`data/`)

Define el modelo de datos inmutable utilizando Kotlin *Data Classes*:

```kotlin
// data/Product.kt
data class Product(
    val id: Int = 0,
    val name: String,
    val price: Double,
    val description: String,
    val category: String,
    val imageUri: String // Ruta local o URL simulada de la imagen
)

```

### 3.2. Capa de Repositorio (`repository/`)

Aplica el patrón repositorio para desacoplar el origen de datos del ViewModel. Implementaremos una simulación CRUD local interactiva en memoria:

```kotlin
// repository/ProductRepository.kt
class ProductRepository {
    private val _products = mutableListOf(
        Product(1, "Laptop", 3500.0, "Laptop gamer de alta gama", "Tecnología", ""),
        Product(2, "Mouse", 80.0, "Mouse ergonómico inalámbrico", "Tecnología", ""),
        Product(3, "Teclado", 150.0, "Teclado mecánico RGB", "Tecnología", "")
    )
    private var currentId = 4

    fun getProducts(): List<Product> = _products.toList()

    fun getProductById(id: Int): Product? = _products.find { it.id == id }

    fun addProduct(product: Product) {
        _products.add(product.copy(id = currentId++))
    }

    fun updateProduct(updatedProduct: Product) {
        val index = _products.indexOfFirst { it.id == updatedProduct.id }
        if (index != -1) {
            _products[index] = updatedProduct
        }
    }

    fun deleteProduct(id: Int) {
        _products.removeAll { it.id == id }
    }
}

```

### 3.3. UI State Pattern (`viewmodel/`)

Usa un `sealed class` para representar de forma explícita y segura los estados de la pantalla:

```kotlin
// viewmodel/ProductUiState.kt
sealed class ProductUiState {
    object Loading : ProductUiState()
    data class Success(val products: List<Product>) : ProductUiState()
    data class Error(val message: String) : ProductUiState()
}

```

### 3.4. El ViewModel y su Factory (`viewmodel/`)

Gestiona el estado elevándolo desde la UI (*State Hoisting*). Incluye las funciones del CRUD y la lógica de filtrado por búsqueda:

```kotlin
// viewmodel/ProductViewModel.kt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuusuario.productapp.data.Product
import com.tuusuario.productapp.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductViewModel(private val repository: ProductRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductUiState>(ProductUiState.Loading)
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadProducts()
    }

    fun loadProducts() {
        _uiState.value = ProductUiState.Loading
        try {
            val allProducts = repository.getProducts()
            val query = _searchQuery.value
            val filtered = if (query.isEmpty()) {
                allProducts
            } else {
                allProducts.filter { 
                    it.name.contains(query, ignoreCase = true) || 
                    it.category.contains(query, ignoreCase = true)
                }
            }
            _uiState.value = ProductUiState.Success(filtered)
        } catch (e: Exception) {
            _uiState.value = ProductUiState.Error("Error al cargar productos")
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
        loadProducts() // Actualiza la lista al escribir
    }

    fun saveProduct(product: Product) {
        if (product.id == 0) {
            repository.addProduct(product)
        } else {
            repository.updateProduct(product)
        }
        loadProducts()
    }

    fun removeProduct(id: Int) {
        repository.deleteProduct(id)
        loadProducts()
    }
}

```

Para pasarle correctamente el repositorio como parámetro al constructor del `ViewModel`, necesitas obligatoriamente una fábrica (`ViewModelProvider.Factory`):

```kotlin
// viewmodel/ProductViewModelFactory.kt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tuusuario.productapp.repository.ProductRepository

class ProductViewModelFactory(private val repository: ProductRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

```

---

## 4. Indicaciones para la UI y Completar el Entregable CRUD

Para finalizar la última sección del laboratorio, debes orquestar la navegación y las diferentes vistas usando **Jetpack Navigation para Compose**.

### Paso 1: Configurar las Rutas de Navegación

Crea una estructura de navegación en tu `MainActivity` o en un Composable raíz que gestione el intercambio de estados de las vistas:

```kotlin
// ui/AppNavigation.kt
sealed class Screen(val route: String) {
    object Catalog : Screen("catalog")
    object Form : Screen("form?productId={productId}") {
        fun createRoute(productId: Int?) = "form?productId=$productId"
    }
    object Detail : Screen("detail/{productId}") {
        fun createRoute(productId: Int) = "detail/$productId"
    }
}

```

### Paso 2: Crear la UI del Catálogo con Lista Optimizada (`LazyColumn`)

Implementa la barra de búsqueda y renderiza los elementos eficientemente mediante un `LazyColumn`:

```kotlin
// ui/ProductScreen.kt
@Composable
fun ProductScreen(
    viewModel: ProductViewModel,
    onNavigateToForm: (Int?) -> Unit,
    onNavigateToDetail: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToForm(null) }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // Barra de Búsqueda
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                label = { Text("Buscar por nombre o categoría") },
                modifier = Modifier.fillMaxWidth().padding(8.md)
            )

            // Manejo de Estados de la UI (UI State Pattern)
            when (val state = uiState) {
                is ProductUiState.Loading -> CircularProgressIndicator()
                is ProductUiState.Error -> Text(state.message)
                is ProductUiState.Success -> {
                    LazyColumn {
                        items(state.products) { product ->
                            ProductItem(
                                product = product,
                                onClick = { onNavigateToDetail(product.id) },
                                onDelete = { viewModel.removeProduct(product.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

```

### Paso 3: Tarjeta de Producto Reutilizable (`ProductItem`)

Diseña un Composable limpio que use un contenedor de tipo `Card` para cumplir con las pautas de UI del laboratorio:

```kotlin
// ui/ProductItem.kt
@Composable
fun ProductItem(product: Product, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.md).clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.md)
    ) {
        Row(modifier = Modifier.padding(16.md), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(text = product.name, style = MaterialTheme.typography.titleLarge)
                [cite_start]Text(text = "S/. ${product.price}", color = Color.Gray) [cite: 688]
                Text(text = "Categoría: ${product.category}", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
            }
        }
    }
}

```

### Paso 4: El Formulario (Create / Update)

Crea la pantalla `ProductFormScreen.kt` que verifique si se recibió un `productId`. Si existe un `productId`, carga los datos en los campos para permitir su **edición** ; si es `null`, mantén los campos limpios para **crear** un nuevo registro. Al presionar el botón guardar, invoca `viewModel.saveProduct(nuevoModificadoProducto)` y regresa a la pantalla anterior con `navController.popBackStack()`.

---

## 5. Criterios de Evaluación Obligatorios para la Entrega

Asegúrate de comprobar los siguientes requisitos técnicos antes de enviar el proyecto:

1.
**SOLID:** El repositorio debe inyectarse a través del Factory del ViewModel.


2.
**State Hoisting:** Ninguna función de la interfaz debe mutar la lista o cambiar los campos de datos de forma directa; todo evento pasa por el `ProductViewModel`.


3.
**Navegación Fluida:** El flujo CRUD completo debe operar sin cierres inesperados utilizando las rutas tipadas de Compose Navigation.