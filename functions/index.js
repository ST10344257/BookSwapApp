// Import v2 specific triggers
const {onDocumentCreated} = require("firebase-functions/v2/firestore");

// Import v2 admin modules
const {getMessaging} = require("firebase-admin/messaging");
const {getFirestore} = require("firebase-admin/firestore");
const {initializeApp} = require("firebase-admin/app");

// Initialize the app
initializeApp();

/**
 * FEATURE 1: Notify Buyer and Seller on new transaction (Order Status)
 * Triggers when a new document is created in the 'transactions' collection
 */
exports.notifyOnTransactionCreated = onDocumentCreated("transactions/{transactionId}", async (event) => {
  // Get the new transaction data
  const snapshot = event.data;
  if (!snapshot) {
    console.log("No data associated with the event");
    return;
  }
  const transaction = snapshot.data();
  const bookTitle = transaction.bookTitle;
  const sellerId = transaction.sellerId;
  const buyerId = transaction.buyerId;

  const notificationTasks = [];

  // --- Task 1: Notify SELLER ---
  const sellerDoc = await getFirestore()
    .collection("users")
    .doc(sellerId)
    .get();

  if (sellerDoc.exists && sellerDoc.data().fcmToken) {
    const sellerToken = sellerDoc.data().fcmToken;
    const sellerPayload = {
      notification: {
        title: "Your Book Sold! 💰",
        body: `Congratulations! Your book "${bookTitle}" has been sold.`,
      },
    };
    // Add the send task to our list
    notificationTasks.push(
      getMessaging().sendToDevice(sellerToken, sellerPayload)
    );
    console.log("Queued notification for SELLER:", sellerId);
  }

  // --- Task 2: Notify BUYER (Checkout Confirmation) ---
  const buyerDoc = await getFirestore()
    .collection("users")
    .doc(buyerId)
    .get();

  if (buyerDoc.exists && buyerDoc.data().fcmToken) {
    const buyerToken = buyerDoc.data().fcmToken;
    const buyerPayload = {
      notification: {
        title: "Order Confirmed! ✅",
        body: `Your purchase of "${bookTitle}" is confirmed. You can track it in 'Track Orders'.`,
      },
    };
    // Add the send task to our list
    notificationTasks.push(
      getMessaging().sendToDevice(buyerToken, buyerPayload)
    );
    console.log("Queued notification for BUYER:", buyerId);
  }

  // Wait for all notifications to be sent
  if (notificationTasks.length > 0) {
    await Promise.all(notificationTasks);
  }
  return;
});

/**
 * FEATURE 2: Notify users of new books in their favorite categories
 * Triggers when a new document is created in the 'books' collection
 */
exports.notifyOnNewBook = onDocumentCreated("books/{bookId}", async (event) => {
  const snapshot = event.data;
  if (!snapshot) {
    console.log("No data associated with the event");
    return;
  }
  const book = snapshot.data();

  // Only notify for user-listed, available books
  if (book.status !== "AVAILABLE" || book.isGoogleBook) {
    console.log("Book is not a new user listing. No notification.");
    return;
  }

  const category = book.category; // e.g., "TECH"
  if (!category) {
    console.log("Book has no category. No notification.");
    return;
  }

  const payload = {
    notification: {
      title: `New ${category} Book! 📚`,
      body: `A new book, "${book.title}", was just listed for R${book.price}.`,
    },
  };

  console.log(`Sending notification to topic: ${category}`);
  // This sends to *everyone* subscribed to the "TECH" topic.
  return getMessaging().sendToTopic(category, payload);
});

/**
 * FEATURE 3: Notify user of a new chat message
 * Triggers when a new document is created in a 'messages' subcollection
 */
exports.notifyOnNewMessage = onDocumentCreated("chats/{chatRoomId}/messages/{messageId}", async (event) => {
  const snapshot = event.data;
  if (!snapshot) {
    console.log("No data associated with the event");
    return;
  }
  const message = snapshot.data();

  const senderName = message.senderName;
  const messageText = message.text;
  const receiverId = message.receiverId;

  // Don't send a notification to yourself
  if (message.senderId === receiverId) {
      return;
  }

  // Get the receiver's user document
  const userDoc = await getFirestore()
    .collection("users")
    .doc(receiverId)
    .get();

  if (!userDoc.exists || !userDoc.data().fcmToken) {
    console.log("No token found for receiver:", receiverId);
    return;
  }

  const fcmToken = userDoc.data().fcmToken;

  const payload = {
    notification: {
      title: `New Message from ${senderName}`,
      body: messageText,
    },
  };

  console.log(`Sending message notification to user: ${receiverId}`);
  return getMessaging().sendToDevice(fcmToken, payload);
});