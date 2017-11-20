#pragma once
#include "MG_Common.h"

#include <vector>
#include <string>

typedef struct {

    std::vector<char> (*embed_string) (const std::vector<unsigned char> img, const std::string str2embed);
    std::vector<char> (*embed_str_and_encode_with_custom_code)(const std::vector<unsigned char> img, const std::string str2embed,
                                                               bool embed, bool encode, unsigned int key);
    std::vector<unsigned char> (*compress_jpeg)(const unsigned char* img_data, size_t width, size_t height,
                                                int quality);
    std::string (*gen_result_file)(std::vector<std::vector<unsigned char> > inputs, std::vector<unsigned char> &buffer);
    std::string (*sha1hash) (const unsigned char* data, size_t len);
    std::string (*encode_delta)(const std::string delta);
    std::string (*base_64)(const char* data, size_t length);

} MG_INNERUTILS_API_FUNCTIONS_TYPE;

extern MG_EXPORT MG_INNERUTILS_API_FUNCTIONS_TYPE mg_utils;

