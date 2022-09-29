#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <random.h>
#include "coap-engine.h"

#define BUFFER_SIZE 200

static void res_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
/*
 * A handler function named [resource name]_handler must be implemented for each RESOURCE.
 * A buffer for the response payload is provided through the buffer pointer. Simple resources can ignore
 * preferred_size and offset, but must respect the REST_MAX_CHUNK_SIZE limit for the buffer.
 * If a smaller block size is requested for CoAP, the REST framework automatically splits the data.
 */
RESOURCE(res_ac,
         "title=\"Air conditioner actuator\";rt=\"airconditioner-c\";if=\"actuator\"",
         res_get_handler,
         NULL,
         res_put_handler,
         NULL);

static float airconditioner_value = 22.0f;


// Used when the client wants to retrieve the current value of the actuator
static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){
  char message[BUFFER_SIZE];

  snprintf(message, BUFFER_SIZE, "{\"actuator_value\": %g}", airconditioner_value);
  int length = strlen(message);

  // Copy the response in the transmission buffer
  memcpy(buffer, message, length);

  // Prepare the response
  coap_set_header_content_format(response, APPLICATION_JSON);
  coap_set_payload(response, buffer, length);
}

/*
 Used when the client has set a new value for the actuator
*/
static void res_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){
     char response_payload[BUFFER_SIZE];

  const char *new_ac_value = NULL;
  coap_get_post_variable(request, "set_ac_value", &new_ac_value);
  airconditioner_value = atof(new_ac_value);

  snprintf(response_payload, BUFFER_SIZE, "{\"new_ac_value\": %g}", airconditioner_value);
  int length = strlen(response_payload);

  // Copy the response in the transmission buffer
  memcpy(buffer, response, length);

  // Prepare the response
  coap_set_header_content_format(response, APPLICATION_JSON);
  coap_set_status_code(response,CHANGED_2_04);
  coap_set_payload(response, response_payload, length);
}
