package june4;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ProductManager {

  private Map<String, Product> products;  // veritabanı

  private Map<String, Supplier<String>> orderNumberSuppliers;

  private List<Order> orders;

  public ProductManager() {
    products = new HashMap<>();
    orderNumberSuppliers = new HashMap<>();
    orders = new ArrayList<>();
  }

  public void addProduct(Product product) {
    products.put(product.getId(), product);
  }

  public Product getProductById(String productId) {
    return products.get(productId);
  }

  public List<Product> filterProducts(Predicate<Product> filterPredicate) {
    // products.values().stream().filter(product->product.getName().startsWith("A")).toList();
    return products.values().stream().filter(filterPredicate).toList();
  }

  // BiConsumer
  // manager.updateStock("1", 20, (product, quantity) -> {
  //  int newStock = product.getStock() + quantity;
  //  product.setStock(newStock);
  //});
  public void updateStock(String productId, int quantity, BiConsumer<Product, Integer> updateFunction) {
    Product productById = getProductById(productId);
    if (productById != null) {
      updateFunction.accept(productById, quantity);
      System.out.println("Stock updated successfully");
    } else {
      System.out.println("Product not found");
    }
  }

  // calculate total value
  //double totalValue = manager.calculateTotalValue(product -> product.getPrice() * product.getStock());
  public double calculateTotalValue(Function<Product, Double> valueFunction) {
    return products.values().stream().mapToDouble(valueFunction::apply).sum();
  }

  public void registerOrderNumberSupplier(String supplierId, Supplier<String> supplier) {
    orderNumberSuppliers.put(supplierId, supplier);
  }

  //generate Order number
  public String generateOrderNumber(String supplierId) {
    Supplier<String> supplier = orderNumberSuppliers.get(supplierId);
    if (supplier != null) {
      return supplier.get();
    } else {
      return "Supplier not found";
    }
  }

  // 0001  1  10
  // 0001  1  10
  // 0001  1  0
  // OrderItem  -> String productId, Integer quantity;
  public void processOrder(String orderId, Map<String, Integer> orderItems,
                           BiConsumer<Product, Integer> updateStockFunction) {
    Map<Product, Integer> productQuantityMap = new HashMap<>();
    for (Map.Entry<String, Integer> entry : orderItems.entrySet()) {
      String productId = entry.getKey();
      Integer quantity = entry.getValue();
      Product product = getProductById(productId);
      if (product != null) {
        updateStock(productId, quantity, updateStockFunction);
        productQuantityMap.compute(product, (key, value) -> {
          if (value == null) {
            return quantity;
          } else {
            return value + quantity;
          }
        });
      }
    }
    Order order = new Order(orderId, productQuantityMap);
    orders.add(order);

    System.out.println("Order processed successfully. Order ID: " + order.getOrderId());
    System.out.println("Ordered products:");
    order.getOrderDetails();
    System.out.println("Total Amount: " + order.getTotalAmount());
  }

  public List<Product> getActiveProductsSortedByPrice() {
	  return products.values()
	          .stream()
	          .filter(product -> product.getProductStatus() == ProductStatus.ACTIVE)
	          .sorted(Comparator.comparingDouble(Product::getPrice))
	          .collect(Collectors.toList());
	}

	public double calculateAveragePriceInCategory(String category) {
	  OptionalDouble average = products.values()
	          .stream()
	          .filter(product -> product.getCategory().equals(category))
	          .mapToDouble(Product::getPrice)
	          .average();
	  return average.orElse(0.0);
	}

	public Map<String, Double> getCategoryPriceSum() {
	  return products.values()
	          .stream()
	          .collect(Collectors.groupingBy(Product::getCategory, Collectors.summingDouble(product -> product.getPrice() * product.getStock())));
	}
}