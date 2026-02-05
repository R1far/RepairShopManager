package com.repairshop.manager.firebase;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.repairshop.manager.models.StockItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс для работы со складом в базе данных Firebase
 * Реализованы функции: просмотр, поиск, создание, обновление и удаление товаров
 */
public class FirebaseStockManager {
    
    // Ссылка на базу данных Firebase
    private FirebaseFirestore firestore;
    // Ссылка на коллекцию "stock" (склад) в базе данных
    private CollectionReference stockCollection;
    
    // Конструктор класса - инициализируется подключение к базе данных
    public FirebaseStockManager() {
        this.firestore = FirebaseHelper.getInstance().getFirestore();
        this.stockCollection = firestore.collection("stock");
    }
    
    /**
     * Создание нового товара на складе
     * Добавление товара в базу данных
     */
    public Task<String> createStockItem(StockItem item) {
        TaskCompletionSource<String> taskSource = new TaskCompletionSource<>();
        
        // Добавлен новый товар в коллекцию
        stockCollection.add(item).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Получен ID созданного документа
                String documentId = task.getResult().getId();
                taskSource.setResult(documentId);
            } else {
                // Установлена ошибка при неудаче
                Exception error = task.getException();
                if (error != null) {
                    taskSource.setException(error);
                } else {
                    taskSource.setException(new Exception("Ошибка создания товара"));
                }
            }
        });
        
        return taskSource.getTask();
    }
    
    /**
     * Обновление информации о товаре
     * Доступно только администратору
     */
    public Task<Void> updateStockItem(StockItem item) {
        TaskCompletionSource<Void> taskSource = new TaskCompletionSource<>();
        
        // Получена ссылка на документ товара по ID
        stockCollection.document(item.getItemId()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                
                if (document.exists()) {
                    // Установлены обновленные значения полей
                    document.getReference()
                        .update("articleNumber", item.getArticleNumber())
                        .continueWith(updateTask -> {
                            document.getReference().update("itemName", item.getItemName());
                            return null;
                        })
                        .continueWith(updateTask -> {
                            document.getReference().update("quantity", item.getQuantity());
                            return null;
                        })
                        .continueWith(updateTask -> {
                            document.getReference().update("price", item.getPrice());
                            return null;
                        })
                        .continueWith(updateTask -> {
                            // Обновлено время последнего изменения
                            document.getReference().update("updatedAt", FieldValue.serverTimestamp());
                            return null;
                        })
                        .addOnCompleteListener(finalTask -> {
                            if (finalTask.isSuccessful()) {
                                taskSource.setResult(null);
                            } else {
                                Exception error = finalTask.getException();
                                if (error != null) {
                                    taskSource.setException(error);
                                } else {
                                    taskSource.setException(new Exception("Ошибка обновления"));
                                }
                            }
                        });
                } else {
                    taskSource.setException(new Exception("Товар не найден"));
                }
            } else {
                Exception error = task.getException();
                if (error != null) {
                    taskSource.setException(error);
                } else {
                    taskSource.setException(new Exception("Ошибка чтения данных"));
                }
            }
        });
        
        return taskSource.getTask();
    }
    
    /**
     * Получение информации о товаре по его ID
     */
    public Task<StockItem> getStockItemById(String itemId) {
        TaskCompletionSource<StockItem> taskSource = new TaskCompletionSource<>();
        
        // Выполнен запрос к базе данных для получения товара
        stockCollection.document(itemId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                
                // Проверено существование документа
                if (document.exists()) {
                    // Преобразован документ в объект StockItem
                    StockItem item = document.toObject(StockItem.class);
                    
                    if (item != null) {
                        // Установлен ID товара
                        item.setItemId(document.getId());
                        taskSource.setResult(item);
                    } else {
                        taskSource.setResult(null);
                    }
                } else {
                    taskSource.setResult(null);
                }
            } else {
                taskSource.setResult(null);
            }
        });
        
        return taskSource.getTask();
    }
    
    /**
     * Получение списка всех товаров на складе
     * Товары отсортированы по названию
     */
    public Task<List<StockItem>> getAllStockItems() {
        TaskCompletionSource<List<StockItem>> taskSource = new TaskCompletionSource<>();
        
        // Выполнен запрос с сортировкой по названию
        stockCollection.orderBy("itemName", Query.Direction.ASCENDING)
            .get()
            .addOnCompleteListener(task -> {
                // Создан список для хранения товаров
                List<StockItem> items = new ArrayList<>();
                
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    
                    // Перебраны все документы из результата запроса
                    for (int i = 0; i < querySnapshot.getDocuments().size(); i++) {
                        DocumentSnapshot document = querySnapshot.getDocuments().get(i);
                        
                        // Преобразован документ в объект StockItem
                        StockItem item = document.toObject(StockItem.class);
                        
                        if (item != null) {
                            // Установлен ID товара
                            item.setItemId(document.getId());
                            // Добавлен товар в список
                            items.add(item);
                        }
                    }
                }
                
                taskSource.setResult(items);
            });
        
        return taskSource.getTask();
    }
    
    /**
     * Поиск товаров по названию или артикулу
     * Поиск выполняется без учета регистра
     */
    public List<StockItem> searchStockItems(List<StockItem> items, String query) {
        // Создан список для результатов поиска
        List<StockItem> results = new ArrayList<>();
        
        // Преобразован поисковый запрос в нижний регистр
        String searchQuery = query.toLowerCase();
        
        // Перебраны все товары из списка
        for (int i = 0; i < items.size(); i++) {
            StockItem item = items.get(i);
            
            // Получены артикул и название товара (защита от null)
            String articleNumber = "";
            if (item.getArticleNumber() != null) {
                articleNumber = item.getArticleNumber().toLowerCase();
            }
            
            String itemName = "";
            if (item.getItemName() != null) {
                itemName = item.getItemName().toLowerCase();
            }
            
            // Проверено совпадение с артикулом
            boolean foundInArticle = false;
            if (articleNumber.contains(searchQuery)) {
                foundInArticle = true;
            }
            
            // Проверено совпадение с названием
            boolean foundInName = false;
            if (itemName.contains(searchQuery)) {
                foundInName = true;
            }
            
            // Добавлен товар в результаты, если найдено совпадение
            if (foundInArticle || foundInName) {
                results.add(item);
            }
        }
        
        return results;
    }
    
    /**
     * Увеличение количества товара на складе (приход товара)
     */
    public Task<Void> increaseQuantity(String itemId, int quantityToAdd) {
        TaskCompletionSource<Void> taskSource = new TaskCompletionSource<>();
        
        // Получен товар из базы данных
        stockCollection.document(itemId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                
                // Проверено существование товара
                if (document.exists()) {
                    // Преобразован документ в объект
                    StockItem item = document.toObject(StockItem.class);
                    
                    if (item != null) {
                        // Установлен ID товара
                        item.setItemId(document.getId());
                        
                        // Рассчитано новое количество
                        int currentQuantity = item.getQuantity();
                        int newQuantity = currentQuantity + quantityToAdd;
                        
                        // Обновлено количество в базе данных
                        document.getReference()
                            .update("quantity", newQuantity)
                            .continueWith(updateTask -> {
                                // Обновлено время последнего изменения
                                document.getReference().update("updatedAt", FieldValue.serverTimestamp());
                                return null;
                            })
                            .continueWith(updateTask -> {
                                // Сохранен ID товара (на случай если отсутствовал)
                                document.getReference().update("itemId", itemId);
                                return null;
                            })
                            .addOnCompleteListener(finalTask -> {
                                if (finalTask.isSuccessful()) {
                                    taskSource.setResult(null);
                                } else {
                                    Exception error = finalTask.getException();
                                    if (error != null) {
                                        taskSource.setException(error);
                                    } else {
                                        taskSource.setException(new Exception("Ошибка обновления"));
                                    }
                                }
                            });
                    } else {
                        taskSource.setException(new Exception("Ошибка чтения товара"));
                    }
                } else {
                    // Товар не существует - невозможно добавить количество
                    taskSource.setResult(null);
                }
            } else {
                Exception error = task.getException();
                if (error != null) {
                    taskSource.setException(error);
                } else {
                    taskSource.setException(new Exception("Ошибка доступа к складу"));
                }
            }
        });
        
        return taskSource.getTask();
    }

    /**
     * Уменьшение количества товара на складе (списание товара)
     * Проверяется достаточность товара перед списанием
     */
    public Task<Void> decreaseQuantity(String itemId, int quantityToDeduct) {
        TaskCompletionSource<Void> taskSource = new TaskCompletionSource<>();
        
        // Получен товар из базы данных
        stockCollection.document(itemId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                
                // Проверено существование товара
                if (document.exists()) {
                    // Преобразован документ в объект
                    StockItem item = document.toObject(StockItem.class);
                    
                    if (item != null) {
                        // Установлен ID товара
                        item.setItemId(document.getId());
                        
                        // Получено текущее количество товара
                        int currentQuantity = item.getQuantity();
                        
                        // Проверено достаточно ли товара для списания
                        if (currentQuantity < quantityToDeduct) {
                            // Создано сообщение об ошибке
                            String errorMessage = "Недостаточно товара: " + item.getItemName() + 
                                ". На складе: " + currentQuantity + ", Требуется: " + quantityToDeduct;
                            taskSource.setException(new Exception(errorMessage));
                            return;
                        }
                        
                        // Рассчитано новое количество
                        int newQuantity = currentQuantity - quantityToDeduct;
                        
                        // Обновлено количество в базе данных
                        document.getReference()
                            .update("quantity", newQuantity)
                            .continueWith(updateTask -> {
                                // Обновлено время последнего изменения
                                document.getReference().update("updatedAt", FieldValue.serverTimestamp());
                                return null;
                            })
                            .continueWith(updateTask -> {
                                // Сохранен ID товара
                                document.getReference().update("itemId", itemId);
                                return null;
                            })
                            .addOnCompleteListener(finalTask -> {
                                if (finalTask.isSuccessful()) {
                                    taskSource.setResult(null);
                                } else {
                                    Exception error = finalTask.getException();
                                    if (error != null) {
                                        taskSource.setException(error);
                                    } else {
                                        taskSource.setException(new Exception("Ошибка обновления"));
                                    }
                                }
                            });
                    } else {
                        taskSource.setException(new Exception("Ошибка чтения товара"));
                    }
                } else {
                    taskSource.setException(new Exception("Товар не найден"));
                }
            } else {
                Exception error = task.getException();
                if (error != null) {
                    taskSource.setException(error);
                } else {
                    taskSource.setException(new Exception("Ошибка чтения данных"));
                }
            }
        });
        
        return taskSource.getTask();
    }
    
    /**
     * Удаление товара со склада
     * Доступно только администратору
     */
    public Task<Void> deleteStockItem(String itemId) {
        // Удален документ товара из базы данных
        return stockCollection.document(itemId).delete();
    }
}
