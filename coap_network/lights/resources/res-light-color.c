#include <stdlib.h>
#include <string.h>
#include "contiki.h"
#include "coap-engine.h"
#include "dev/leds.h"

#include "sys/log.h"
#define LOG_MODULE "light-color"
#define LOG_LEVEL LOG_LEVEL_APP

#include "global-variables.h"

static void res_light_post_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

RESOURCE(res_light_color,
         "title=\"Light color: ?color=r|g|y\";rt=\"Control\"",
         NULL,
         res_light_post_handler,
         NULL,
         NULL);

uint8_t led = LEDS_YELLOW;

static void
res_light_post_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){

  size_t len = 0;
  const char *color = NULL;
  int success = 1;
 
 
  len = coap_get_query_variable(request, "color", &color); 
  if(len > 0 && len < 7) {

    if(strncmp(color, "y", len) == 0) {
      led = LEDS_YELLOW;
    } else if(strncmp(color, "g", len) == 0) {
      led = LEDS_GREEN;
    } else if(strncmp(color, "r", len) == 0) {
      led = LEDS_RED;
    } else {
      success = 0;
    }
    if(success) {
      LOG_DBG("Color %.*s\n", (int)len, color);

      if(light_on) {
	leds_off(LEDS_ALL);
	leds_on(LEDS_NUM_TO_MASK(led));
      } 
    } 
  } else {
        success = 0;
    }
    if(!success) {
       coap_set_status_code(response, BAD_REQUEST_4_00);
    }
}


