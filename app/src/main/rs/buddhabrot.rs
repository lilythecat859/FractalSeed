#pragma version(1)
#pragma rs java_package_name(com.lilythecat859.fractalseed)

int width;
int height;
int maxIter;
long seed;

static float3 palette(int n) {
    uint32_t h = (uint32_t)(n * 0x9e3779b9 + (uint32_t)seed);
    float r = (h & 0xFF) / 255.0f;
    float g = ((h >> 8) & 0xFF) / 255.0f;
    float b = ((h >> 16) & 0xFF) / 255.0f;
    return (float3){r, g, b};
}

uchar4 __attribute__((kernel)) root(uchar4 in, uint32_t x, uint32_t y) {
    float fx = ((float)x / width) * 3.0f - 2.0f;
    float fy = ((float)y / height) * 3.0f - 1.5f;

    float zr = 0.0f, zi = 0.0f;
    int iter;
    for (iter = 0; iter < maxIter && (zr * zr + zi * zi) <= 4.0f; ++iter) {
        float tmp = zr * zr - zi * zi + fx;
        zi = 2.0f * zr * zi + fy;
        zr = tmp;
    }
    float3 col = palette(iter);
    uchar4 out;
    out.r = (uchar)(col.r * 255);
    out.g = (uchar)(col.g * 255);
    out.b = (uchar)(col.b * 255);
    out.a = 255;
    return out;
}
