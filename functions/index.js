const { onRequest } = require("firebase-functions/v2/https");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");

initializeApp();

exports.enviarNotificacionNuevoProducto = onRequest({ region: "us-central1" }, async (req, res) => {
  const { productoId, nombreProducto } = req.body;

  console.log("Cuerpo de la solicitud:", req.body);

  if (!productoId || !nombreProducto) {
    console.log("Error: Faltan datos requeridos");
    return res.status(400).json({ error: "Faltan datos requeridos" });
  }

  try {
    const clientesSnap = await getFirestore()
      .collection("clientes")
      .where("tipo_usuario", "==", "cliente")
      .get();

    const tokens = [];

    clientesSnap.forEach(doc => {
      const token = doc.get("fcmToken");
      if (token) {
        console.log("Cliente:", doc.id, "Token:", token);
        tokens.push(token);
      }
    });

    if (tokens.length === 0) {
      console.log("Error: No se encontraron tokens FCM válidos");
      return res.status(404).json({ error: "No se encontraron tokens FCM válidos" });
    }

    const payload = {
      notification: {
        title: "Nuevo producto agregado",
        body: nombreProducto,
      },
      data: {
        productoId: productoId,
      },
    };

    console.log("Enviando notificaciones a los tokens:", tokens);

    const results = [];
    let successCount = 0;
    let failureCount = 0;

    // Enviar notificación a cada token individualmente
    for (const token of tokens) {
      try {
        const response = await getMessaging().send({
          token: token,
          notification: payload.notification,
          data: payload.data,
        });
        console.log(`Notificación enviada a ${token}:`, response);
        results.push({ token, success: true, response });
        successCount++;
      } catch (error) {
        console.error(`Error al enviar a ${token}:`, error);
        results.push({ token, success: false, error: error.message });
        failureCount++;
      }
    }

    return res.status(200).json({
      success: true,
      enviados: tokens.length,
      successCount: successCount,
      failureCount: failureCount,
      results: results
    });
  } catch (error) {
    console.error("Error general:", error);
    return res.status(500).json({ error: `Error general: ${error.message}` });
  }
});