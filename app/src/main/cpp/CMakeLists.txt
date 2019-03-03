#指定需要CMAKE的最小版本
cmake_minimum_required(VERSION 3.4.1)

#设置生成的so动态库最后输出的路径
#set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI})

#设置头文件搜索路径（和此txt同个路径的头文件无需设置），可选
INCLUDE_DIRECTORIES(${CMAKE_CURRENT_SOURCE_DIR}/webrtc_ns)

#指定用到的系统库或者NDK库或者第三方库的搜索路径，可选。
#LINK_DIRECTORIES(/usr/local/lib)
#生成多个就继续写add_library，仿造下面的去写就ok
add_library( # Sets the name of the library.
        Webrtc

        # Sets the library as a shared library.
        SHARED

        #提供的c++文件
        webrtc.cpp
        webrtc_ns/complex_bit_reverse.c
        webrtc_ns/complex_fft.c
        webrtc_ns/copy_set_operations.c
        webrtc_ns/cross_correlation.c
        webrtc_ns/division_operations.c
        webrtc_ns/downsample_fast.c
        webrtc_ns/energy.c
        webrtc_ns/fft4g.c
        webrtc_ns/get_scaling_square.c
        webrtc_ns/min_max_operations.c
        webrtc_ns/noise_suppression_x.c
        webrtc_ns/noise_suppression.c
        webrtc_ns/ns_core.c
        webrtc_ns/nsx_core_c.c
        webrtc_ns/nsx_core_neon_offsets.c
        webrtc_ns/nsx_core.c
        webrtc_ns/real_fft.c
        webrtc_ns/resample_48khz.c
        webrtc_ns/resample_by_2_internal.c
        webrtc_ns/resample_by_2_mips.c
        webrtc_ns/resample_by_2.c
        webrtc_ns/resample_fractional.c
        webrtc_ns/resample.c
        webrtc_ns/ring_buffer.c
        webrtc_ns/spl_init.c
        webrtc_ns/spl_sqrt_floor.c
        webrtc_ns/spl_sqrt.c
        webrtc_ns/splitting_filter.c
        webrtc_ns/vector_scaling_operations.c
        )

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        Webrtc

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})