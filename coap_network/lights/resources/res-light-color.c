#include <stdlib.h>
#include <string.h>
#include "contiki.h"
#include "coap-engine.h"
#include "dev/leds.h"

#include "sys/log.h"
#define LOG_MODULE "light-color"
#define LOG_LEVEL LOG_LEVEL_APP

#include "global-variables.h"

static void res_light_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

RESOURCE(res_light_color,
         "title=\"Light color: ?color=r|g|y\";rt=\"Control\"",
         NULL,
         NULL,
         res_light_put_handler,
         NULL);

uint8_t led = LEDS_GREEN;

static void
res_light_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){

  size_t len = 0;
  const char *text = NULL;
  int success = 1;
  char color[7];
  memset(color, 0, 7);
 
  len = coap_get_post_variable(request, "color", &text);
  memcpy(color, text, len);

  if(len > 0 && len < 7) {

    if(strncmp(color, "b", len) == 0) {
      led = LEDS_BLUE;
    } else if(strncmp(color, "g", len) == 0) {
      led = LEDS_GREEN;
    } else if(strncmp(color, "r", len) == 0) {
      led = LEDS_RED;
    } else {
      success = 0;
    }
    if(success) {
      LOG_INFO("Color %.*s\n", (int)len, color);

      if(light_on) {
	leds_set(LEDS_NUM_TO_MASK(led));
      } 
    } 
  } else {
        success = 0;
    }
    if(!success) {
       coap_set_status_code(response, BAD_REQUEST_4_00);
    }
}


