#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <random.h>
#include "coap-engine.h"

#define BUFFER_SIZE 200

static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_event_handler(void);

/*
 * A handler function named [resource name]_handler must be implemented for each RESOURCE.
 * A buffer for the response payload is provided through the buffer pointer. Simple resources can ignore
 * preferred_size and offset, but must respect the REST_MAX_CHUNK_SIZE limit for the buffer.
 * If a smaller block size is requested for CoAP, the REST framework automatically splits the data.
 */
EVENT_RESOURCE(res_light,
         "title=\"Light Sensor\";rt=\"light-c\";if=\"sensor\";obs",
         res_get_handler,
         res_get_handler,
         NULL,
         NULL,
         res_event_handler);

static float sensor_value = 0.0f;

static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){
  char message[BUFFER_SIZE];

  snprintf(message, BUFFER_SIZE, "{\"sensor_value\": %g}", sensor_value);
  printf("Sending %s/n", message);
  int length = strlen(message);

  // Copy the response in the transmission buffer
  memcpy(buffer, message, length);

  // Prepare the response
  coap_set_header_content_format(response, APPLICATION_JSON);
  //coap_set_header_etag(response, (uint8_t *)&length, 1);
  coap_set_payload(response, buffer, length);
}

static void res_event_handler(void){

     sensor_value += random_rand() % 40;
     // Notify all the observers
     coap_notify_observers(&res_light);
}
