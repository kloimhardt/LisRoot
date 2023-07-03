// ferret-lisp 0.4.0-171a575
        #include "TCanvas.h"
        #include "TF1.h"

        #ifndef FERRET_RUNTIME_H
        #define FERRET_RUNTIME_H

         // Detect Hardware
         # define FERRET_CONFIG_SAFE_MODE TRUE

         #if !defined(FERRET_SAFE_MODE)
           #if defined(__APPLE__) ||                       \
             defined(_WIN32) ||                            \
             defined(__linux__) ||                         \
             defined(__unix__) ||                          \
             defined(_POSIX_VERSION)
           
             # undef  FERRET_CONFIG_SAFE_MODE
             # define FERRET_STD_LIB TRUE
           #endif
           
           #if defined(ARDUINO)

             # define FERRET_HARDWARE_ARDUINO TRUE

             #if !defined(FERRET_HARDWARE_ARDUINO_UART_PORT)
               # define FERRET_HARDWARE_ARDUINO_UART_PORT Serial
             #endif
           #endif
           
           #if defined(FERRET_HARDWARE_ARDUINO)
             # undef  FERRET_CONFIG_SAFE_MODE
             # define FERRET_DISABLE_STD_MAIN TRUE
           #endif
         #endif

         #if defined(FERRET_CONFIG_SAFE_MODE)
           # define FERRET_DISABLE_MULTI_THREADING TRUE
           # define FERRET_DISABLE_STD_OUT TRUE
         #endif
         #ifdef FERRET_STD_LIB
          #include <iostream>
          #include <iomanip>
          #include <sstream>
          #include <cstdio>
          #include <cstdlib>
          #include <cstddef>
          #include <cmath>
          #include <vector>
          #include <algorithm>
          #include <chrono>
          #include <atomic>
          #include <mutex>
          #include <thread>
          #include <future>
         #endif

         #ifdef FERRET_HARDWARE_ARDUINO
          #include <Arduino.h>
          #include <stdio.h>
          #include <stdlib.h>
          #include <stdint.h>
         #endif

         #ifdef FERRET_CONFIG_SAFE_MODE
          #include <stdio.h>
          #include <stdlib.h>
          #include <stdint.h>
          #include <math.h>
         #endif

         namespace ferret{
           namespace runtime {}
           namespace rt = runtime;
           // Types
           typedef uint8_t byte;
           #pragma pack(push, 1)
           class int24_t {
           protected:
             byte word[3];
           public:
             int24_t(){ }
           
             template<typename T>
             explicit int24_t( T const & val ) {
               *this   = (int32_t)val;
             }
           
             int24_t( const int24_t& val ) {
               *this   = val;
             }
           
             operator int32_t() const {
               if (word[2] & 0x80) { // negative? - then sign extend.
                 return
                   (int32_t)(((uint32_t)0xff    << 24) |
                             ((uint32_t)word[2] << 16) |
                             ((uint32_t)word[1] << 8)  |
                             ((uint32_t)word[0] << 0));
               }else{
                 return
                   (int32_t)(((uint32_t)word[2] << 16) |
                             ((uint32_t)word[1] << 8)  |
                             ((uint32_t)word[0] << 0));
               }
             }
           
             int24_t& operator =( const int24_t& input ) {
               word[0]   = input.word[0];
               word[1]   = input.word[1];
               word[2]   = input.word[2];
           
               return *this;
             }
           
             int24_t& operator =( const int32_t input ) {
               word[0]   = ((byte*)&input)[0];
               word[1]   = ((byte*)&input)[1];
               word[2]   = ((byte*)&input)[2];
           
               return *this;
             }
           
             int24_t operator +( const int24_t& val ) const {
               return int24_t( (int32_t)*this + (int32_t)val );
             }
           
             int24_t operator -( const int24_t& val ) const {
               return int24_t( (int32_t)*this - (int32_t)val );
             }
           
             int24_t operator *( const int24_t& val ) const {
               return int24_t( (int32_t)*this * (int32_t)val );
             }
           
             int24_t operator /( const int24_t& val ) const {
               return int24_t( (int32_t)*this / (int32_t)val );
             }
           
             int24_t& operator +=( const int24_t& val ) {
               *this   = *this + val;
               return *this;
             }
           
             int24_t& operator -=( const int24_t& val ) {
               *this   = *this - val;
               return *this;
             }
           
             int24_t& operator *=( const int24_t& val ) {
               *this   = *this * val;
               return *this;
             }
           
             int24_t& operator /=( const int24_t& val ) {
               *this   = *this / val;
               return *this;
             }
           
             int24_t operator -() {
               return int24_t( -(int32_t)*this );
             }
           
             bool operator ==( const int24_t& val ) const {
               return (int32_t)*this == (int32_t)val;
             }
           
             bool operator !=( const int24_t& val ) const {
               return (int32_t)*this != (int32_t)val;
             }
           
             bool operator >=( const int24_t& val ) const {
               return (int32_t)*this >= (int32_t)val;
             }
           
             bool operator <=( const int24_t& val ) const {
               return (int32_t)*this <= (int32_t)val;
             }
           
             bool operator >( const int24_t& val ) const {
               return (int32_t)*this > (int32_t)val;
             }
           
             bool operator <( const int24_t& val ) const {
               return (int32_t)*this < (int32_t)val;
             }
           };
           #pragma pack(pop)
           // Concurrency
           #if defined(FERRET_DISABLE_MULTI_THREADING)
             class mutex {
             public:
               void lock()   {} 
               void unlock() {} 
             };
           #else
             #if defined(FERRET_STD_LIB)
               class mutex {
                 ::std::mutex m;
               public:
                 void lock()   { m.lock();   } 
                 void unlock() { m.unlock(); }
               };
             #endif
           
             #if defined(FERRET_HARDWARE_ARDUINO)
               class mutex {
               public:
                 void lock()   { noInterrupts(); } 
                 void unlock() { interrupts();   }
               };
             #endif
           #endif
           
           class lock_guard{
             mutex & _ref;
           public:
             explicit lock_guard(const lock_guard &) = delete;
             explicit lock_guard(mutex & mutex) : _ref(mutex) { _ref.lock(); };
             ~lock_guard() { _ref.unlock(); }
           };
           // Containers
           #undef bit
           
           #if !defined(FERRET_BITSET_WORD_TYPE)
             #define FERRET_BITSET_WORD_TYPE unsigned int
             #if defined(__clang__) || defined(__GNUG__)
               #define FERRET_BITSET_USE_COMPILER_INTRINSICS true
             #endif
           #endif
           
           template<size_t S, typename word_t = FERRET_BITSET_WORD_TYPE>
           class bitset {
             static const size_t bits_per_word = sizeof(word_t) * 8;
             static const size_t n_words = S / bits_per_word;
           
             static_assert((S % bits_per_word) == 0, "bitset size must be a multiple of word_t");
           
             word_t bits[n_words];
           
             inline size_t word (size_t i) const { return i / bits_per_word; }
             inline size_t bit  (size_t i) const { return i % bits_per_word; }
           
           public:
             bitset() : bits{ word_t(0x00) } { }
           
             inline void set   (size_t b){
               bits[word(b)] = (word_t)(bits[word(b)] |  (word_t(1) << (bit(b))));
             }
           
             inline void set (size_t b, size_t e){
               size_t word_ptr = word(b);
               size_t n_bits = e - b;
           
               bits[word_ptr] = (word_t)(bits[word_ptr] | bit_block(bit(b), n_bits));
           
               n_bits -= bits_per_word - bit(b);
               word_ptr++;
               size_t last_word = (word(e) == n_words) ? n_words : word(e) + 1;
               for (; word_ptr < last_word; word_ptr++){
                 bits[word_ptr] = (word_t)(bits[word_ptr] | bit_block(0, n_bits));
                 n_bits -= bits_per_word;
               }
             }
           
             inline void reset (size_t b){
               bits[word(b)] = (word_t)(bits[word(b)] & ~(word_t(1) << (bit(b))));
             }
           
             inline void reset (size_t b, size_t e){
               size_t word_ptr = word(b);
               size_t n_bits = e - b;
           
               bits[word_ptr] = (word_t)(bits[word_ptr] & ~bit_block(bit(b), n_bits));
           
               n_bits -= bits_per_word - bit(b);
               word_ptr++;
               size_t last_word = (word(e) == n_words) ? n_words : word(e) + 1;
               for (; word_ptr < last_word; word_ptr++){
                 bits[word_ptr] = (word_t)(bits[word_ptr] & ~bit_block(0, n_bits));
                 n_bits -= bits_per_word;
               }
             }
           
             inline void flip (size_t b){
               bits[word(b)] = (word_t)(bits[word(b)] ^  (word_t(1) << (bit(b))));
             }
           
             inline void flip (size_t b, size_t e){
               size_t word_ptr = word(b);
               size_t n_bits = e - b;
           
               bits[word_ptr] = (word_t)(bits[word_ptr] ^ bit_block(bit(b), n_bits));
           
               n_bits -= bits_per_word - bit(b);
               word_ptr++;
               size_t last_word = (word(e) == n_words) ? n_words : word(e) + 1;
               for (; word_ptr < last_word; word_ptr++){
                 bits[word_ptr] = (word_t)(bits[word_ptr] ^ bit_block(0, n_bits));
                 n_bits -= bits_per_word;
               }
             }
           
             inline bool test  (size_t b) const {
               return (bits[word(b)] & (word_t(1) << (bit(b))));
             }
           
             inline size_t ffs(size_t b = 0, size_t e = S) const {
           #if defined(FERRET_BITSET_USE_COMPILER_INTRINSICS)
                 // search first word
                 size_t word_ptr = word(b);
                 word_t this_word = bits[word_ptr];
           
                 // mask off bits below bound
                 this_word &= (~static_cast<word_t>(0)) << bit(b);
                 
                 if (this_word != static_cast<word_t>(0))
                   return ((word_ptr * bits_per_word) + (size_t) __builtin_ctz(this_word));
           
                 // check subsequent words
                 word_ptr++;
                 size_t last_word = (word(e) == n_words) ? n_words : word(e) + 1;
                 for (; word_ptr < last_word; word_ptr++){
                   this_word = bits[word_ptr];
                   if (this_word != static_cast<word_t>(0))
                     return ((word_ptr * bits_per_word) + (size_t) __builtin_ctz(this_word));
                 }
           #else
                 for(size_t i = b; i < e; i++)
                   if (test(i))
                     return i;
           #endif
               return S;
             }
           
             inline size_t ffr(size_t b = 0, size_t e = S) const {
           #if defined(FERRET_BITSET_USE_COMPILER_INTRINSICS)
                 // same as ffs but complements word before counting
                 size_t word_ptr = word(b);
                 word_t this_word = ~bits[word_ptr];
           
                 this_word &= (~static_cast<word_t>(0)) << bit(b);
                 
                 if (this_word != static_cast<word_t>(0))
                   return ((word_ptr * bits_per_word) + (size_t) __builtin_ctz(this_word));
           
                 word_ptr++;
                 size_t last_word = (word(e) == n_words) ? n_words : word(e) + 1;
                 for (; word_ptr < last_word; word_ptr++){
                   this_word = ~bits[word_ptr];
                   if (this_word != static_cast<word_t>(0))
                     return ((word_ptr * bits_per_word) + (size_t) __builtin_ctz(this_word));
                 }
           #else
                 for(size_t i = b; i < e; i++)
                   if (!test(i))
                     return i;
           #endif
               return S;
             }
           
             // Return word with length-n bit block starting at bit p set.
             // Both p and n are effectively taken modulo bits_per_word.
             static inline word_t bit_block(size_t p, size_t n){
               if (n >= bits_per_word)
                 return (word_t)(word_t(-1) << p);
           
               word_t x = (word_t)((word_t(1) << n) - word_t(1));
               return  (word_t)(x << p);
             }
           
           #if defined(FERRET_STD_LIB)
             friend std::ostream& operator<< (std::ostream& stream, bitset& x) {
               for(size_t i = 0; i < S; i++)
                 stream << x.test(i);
               return stream;
             }
           #endif
           };
         }

         // Math
         namespace ferret{
           constexpr auto operator "" _MB( unsigned long long const x ) -> long {
             return 1024L * 1024L * (long)x;
           }
             
           constexpr auto operator "" _KB( unsigned long long const x ) -> long {
             return 1024L * (long)x;
           }
             
           constexpr auto operator "" _pi(long double x) -> double {
             return 3.14159265358979323846 * (double)x;
           }
           
           constexpr auto operator "" _pi(unsigned long long int  x) -> double {
             return 1.0_pi * (double)x;
           }
           
           constexpr auto operator "" _deg(long double x) -> double {
             return (1.0_pi * (double)x) / 180;
           }
           
           constexpr auto operator "" _deg(unsigned long long int  x) -> double {
             return 1.0_deg * (double)x;
           }
           #if !defined(__clang__)
           constexpr auto operator "" _QN(long double x) -> int {
             return (int)::floor(::log(1.0/(double)x)/::log(2));
           }
           #endif
           
           template<int bits> struct fixed_real_container;
           template<> struct fixed_real_container<8>  { typedef int8_t  base_type;
                                                        typedef int16_t next_type; };
           template<> struct fixed_real_container<16> { typedef int16_t base_type;
                                                        typedef int24_t next_type; };
           template<> struct fixed_real_container<24> { typedef int24_t base_type;
                                                        typedef int32_t next_type; };
           template<> struct fixed_real_container<32> { typedef int32_t base_type;
                                                        typedef int64_t next_type; };
           template<> struct fixed_real_container<64> { typedef int64_t base_type;
                                                        typedef int64_t next_type; };
           
           #pragma pack(push, 1)
           template<int bits, int exp>
           class fixed_real {
             typedef fixed_real fixed;
             typedef typename fixed_real_container<bits>::base_type base;
             typedef typename fixed_real_container<bits>::next_type next;
           
             base m;
             static const int N      = (exp - 1);
             static const int factor = 1 << N;
           
             template<typename T>
             inline base from(T d) const { return (base)(d * factor); }
           
             template<typename T>
             inline T to_rational() const { return T(m) / factor; }
           
             template<typename T>
             inline T to_whole() const { return (T)(m >> N); }
               
           public:
           
             //from types
             explicit fixed_real( )           : m(0) { }
             template<typename T>
             explicit fixed_real(T v)         : m(from<T>(v)) {}
           
             template<typename T>
             fixed& operator=(T v)        { m = from<T>(v); return *this; }
               
             //to types
             template<typename T>
             operator T()           const { return to_whole<T>();    }
             operator double()      const { return to_rational<double>(); }
               
             // operations
             fixed& operator+= (const fixed& x) { m += x.m; return *this; }
             fixed& operator-= (const fixed& x) { m -= x.m; return *this; }
             fixed& operator*= (const fixed& x) { m = (base)(((next)m * (next)x.m) >> N); return *this; }
             fixed& operator/= (const fixed& x) { m = (base)(((next)m * factor) / x.m); return *this; }
             fixed& operator*= (int x)          { m *= x; return *this; }
             fixed& operator/= (int x)          { m /= x; return *this; }
             fixed  operator-  ( )              { return fixed(-m); }
               
             // friend functions
             friend fixed operator+ (fixed x, const fixed& y) { return x += y; }
             friend fixed operator- (fixed x, const fixed& y) { return x -= y; }
             friend fixed operator* (fixed x, const fixed& y) { return x *= y; }
             friend fixed operator/ (fixed x, const fixed& y) { return x /= y; }
               
             // comparison operators
             friend bool operator== (const fixed& x, const fixed& y) { return x.m == y.m; }
             friend bool operator!= (const fixed& x, const fixed& y) { return x.m != y.m; }
             friend bool operator>  (const fixed& x, const fixed& y) { return x.m > y.m; }
             friend bool operator<  (const fixed& x, const fixed& y) { return x.m < y.m; }
             friend bool operator>= (const fixed& x, const fixed& y) { return x.m >= y.m; }
             friend bool operator<= (const fixed& x, const fixed& y) { return x.m <= y.m; }
           
           #if defined(FERRET_STD_LIB)
             friend std::ostream& operator<< (std::ostream& stream, const fixed& x) {
               stream << (double)x;
               return stream;
             }
           #endif
           };
           #pragma pack(pop)
           #if !defined(FERRET_NUMBER_TYPE)
              #define FERRET_NUMBER_TYPE int
           #endif
           
           #if !defined(FERRET_REAL_TYPE)
              #define FERRET_REAL_TYPE   double
           #endif
           
           #if !defined(FERRET_REAL_EPSILON)
              #define FERRET_REAL_EPSILON   0.0001
           #endif
           
             int req_real_precision(double t) {
               return ((-1 * (int)log10(t)));
             }
           
             typedef FERRET_NUMBER_TYPE  number_t;                   // Whole number Container.
             typedef FERRET_REAL_TYPE    real_t;                     // Real number Container.
             const   real_t              real_epsilon(FERRET_REAL_EPSILON);
             const   int                 real_precision = req_real_precision(FERRET_REAL_EPSILON);
           namespace runtime{
             #undef min
             #undef max
             #undef abs
           
             template <typename T>
             static constexpr T max(T a, T b) {
               return a < b ? b : a;
             }
           
             template <typename T, typename... Ts>
             static constexpr T max(T a, Ts... bs) {
               return max(a, max(bs...));
             }
             
             template<typename T>
             constexpr T min(T a, T b){
               return ((a) < (b) ? (a) : (b));
             }
           
             template <typename T, typename... Ts>
             static constexpr T min(T a, Ts... bs) {
               return min(a, min(bs...));
             }
           
             template<typename T>
             constexpr T abs(T a){
               return ((a) < (T)0 ? -(a) : (a));
             }
           }
         }

         // Initialize Hardware
         namespace ferret{
           #if !defined(FERRET_UART_RATE)
             # define FERRET_UART_RATE 9600
           #endif
           #if !defined(FERRET_IO_STREAM_SIZE)
             # define FERRET_IO_STREAM_SIZE 80
           #endif
           #if defined(FERRET_DISABLE_STD_OUT)
              namespace runtime{
                void init(){ }
               
                template <typename T>
                void print(T){ }
              }
           #endif
           #if defined(FERRET_STD_LIB) && !defined(FERRET_DISABLE_STD_OUT)
             namespace runtime{
               void init(){}
               
               template <typename T>
               void print(const T & t){ std::cout << t; }
           
               template <>
               void print(const real_t & n){
                 std::cout << std::fixed << std::setprecision(real_precision) << n;
               }
           
               void read_line(char *buff, std::streamsize len){
                 std::cin.getline(buff, len);
               }
             }
           #endif
           #if defined(FERRET_HARDWARE_ARDUINO) && !defined(FERRET_DISABLE_STD_OUT) 
             namespace runtime{
               void init(){ FERRET_HARDWARE_ARDUINO_UART_PORT.begin(FERRET_UART_RATE); }
           
               template <typename T>
               void print(const T t){ FERRET_HARDWARE_ARDUINO_UART_PORT.print(t); }
           
               template <>
               // cppcheck-suppress passedByValue
               void print(const real_t d){
                 FERRET_HARDWARE_ARDUINO_UART_PORT.print(double(d), real_precision);
               }
               
               template <>
               void print(void *p){
                 FERRET_HARDWARE_ARDUINO_UART_PORT.print((size_t)p,HEX);
               }
           
               template <> void print(const void * const p){
                 FERRET_HARDWARE_ARDUINO_UART_PORT.print((size_t)p, HEX);
               }
           
               void read_line(char *buff, size_t len){
                 byte idx = 0;
                 char c;
                 do{
                   while (FERRET_HARDWARE_ARDUINO_UART_PORT.available() == 0);
                   c = FERRET_HARDWARE_ARDUINO_UART_PORT.read();
                   buff[idx++] = c;
                 }while (c != '\n');
                 buff[--idx] = 0x00;
               }
              }
           #endif
           #if !defined(FERRET_DISABLE_STD_OUT)
              namespace runtime{
                template <typename T>
                void println(T t){
                  print(t);
                  print((char)0xA);
                }
              }
           #endif
         }

         // Object System Base
         namespace ferret{
           namespace memory {
             template <typename T>
             class pointer{
               T *ptr;
           
             public:
           
               inline explicit pointer(T *p = nullptr) : ptr(p){ }
               inline operator T* () const { return ptr; }
           
               inline pointer& operator= (T *other){
                 ptr = other;
                 return *this;
               }
           
               inline T *operator->() const { return ptr; }
             };
           }
           namespace memory{
             inline size_t align_of(uintptr_t size, size_t align){
               return (size + align - 1) & ~(align - 1);
             }
           
             template<class T>
             size_t align_of(const void * ptr) {
               return align_of(reinterpret_cast<uintptr_t>(ptr), sizeof(T));
             }
               
             inline size_t align_req(uintptr_t size, size_t align){
               size_t adjust = align - (size & (align - 1));
                 
               if(adjust == align)
                 return 0;
               return adjust;
             }
           
             template<class T>
             size_t align_req(const void * ptr) {
               return align_req(reinterpret_cast<uintptr_t>(ptr), sizeof(T));
             }
           
             template <typename... Ts>
             constexpr size_t max_sizeof() {
               return rt::max(sizeof(Ts)...);
             }
           }
           #ifdef FERRET_MEMORY_POOL_SIZE
           namespace memory{
             namespace allocator{
               template<typename page_t, size_t pool_size,
                        typename bitset_word_t = FERRET_BITSET_WORD_TYPE>
               struct memory_pool {
                 bitset<pool_size, bitset_word_t> used;
                 page_t pool[pool_size];
                 size_t next_ptr;
           
                 memory_pool() : pool{0}, next_ptr(0) { }
           
                 inline size_t scan(size_t n_pages, size_t from_page = 0) const {
                   for(;;){
                     size_t begin = used.ffr(from_page);
                     size_t end   = begin + n_pages;
           
                     if (end > pool_size)
                       return pool_size;
           
                     if (used.ffs(begin, end) >= end)
                       return begin;
           
                     from_page = end;
                   }
                 }
           
                 void *allocate(size_t req_size){
                   req_size = align_of(req_size, sizeof(page_t)) + sizeof(page_t);
                   size_t n_pages = req_size / sizeof(page_t);
                   size_t page   = scan(n_pages, next_ptr);
           
                   if (page == pool_size){
                     page = scan(n_pages);
                     if (page == pool_size)
                       return nullptr;
                   }
           
                   pool[page] = (page_t)n_pages;
                   next_ptr = page + n_pages;
                   used.flip(page, next_ptr);
           
                   return &pool[++page];
                 }
           
                 void free(void *p){
                   ptrdiff_t begin = (static_cast<page_t *>(p) - pool) - 1;
                   ptrdiff_t end = begin + (ptrdiff_t)pool[begin];
                   used.flip((size_t)begin, (size_t)end);
                 }
               };
             }
           }
           #endif
           #if defined(FERRET_MEMORY_POOL_SIZE) && !defined(FERRET_ALLOCATOR)
           
            #define FERRET_ALLOCATOR memory::allocator::pool
           
            #if !defined(FERRET_MEMORY_POOL_PAGE_TYPE)
             #define FERRET_MEMORY_POOL_PAGE_TYPE size_t
            #endif
           
           namespace memory{
             namespace allocator{
           
               memory_pool<FERRET_MEMORY_POOL_PAGE_TYPE, FERRET_MEMORY_POOL_SIZE> program_memory;
           
               class pool{
               public:
           
                 static void init(){ }
                 
                 static inline void*  allocate(size_t s){
                   return program_memory.allocate(s);
                 }
           
                 template<typename FT>
                 static inline void* allocate(){ return allocate(sizeof(FT)); }
                 
                 static inline void   free(void * ptr){ program_memory.free(ptr); }
               };
             }
           }
           #endif
           #ifdef FERRET_MEMORY_BOEHM_GC
           
           #define FERRET_ALLOCATOR memory::allocator::gc
           #define FERRET_DISABLE_RC true
           
           #include <gc.h>
           
           namespace memory{
             namespace allocator{
               
               class gc{
               public:
           
                 static void init(){ GC_INIT(); }
                 
                 static inline void* allocate(size_t s){
           #ifdef FERRET_DISABLE_MULTI_THREADING
                   return GC_MALLOC(s);
           #else
                   return GC_MALLOC_ATOMIC(s);
           #endif
                 }
           
                 template<typename FT>
                 static inline void* allocate(){ return allocate(sizeof(FT)); }
               
                 static inline void  free(void * ptr){ }
               };
             }
           }
           #endif
           #if !defined(FERRET_ALLOCATOR)
           
           #define FERRET_ALLOCATOR memory::allocator::system
           
           namespace memory{
             namespace allocator{
           
               class system{
               public:
           
                 static void init(){ }
           
                 static inline void* allocate(size_t s){ return ::malloc(s); }
           
                 template<typename FT>
                 static inline void* allocate(){ return allocate(sizeof(FT)); }
           
                 static inline void  free(void * ptr){ ::free(ptr); } 
               };
             }
           }
           #endif
           namespace memory{
             namespace allocator{
               class synchronized{
                 static mutex lock;
               public:
           
                 static void init(){ FERRET_ALLOCATOR::init(); }
           
                 static inline void* allocate(size_t s){
                   lock_guard guard(lock);
                   return FERRET_ALLOCATOR::allocate(s);
                 }
           
                 template<typename FT>
                 static inline void* allocate(){ return allocate(sizeof(FT)); }
           
                 static inline void  free(void * ptr){
                   lock_guard guard(lock);
                   FERRET_ALLOCATOR::free(ptr);
                 }
               };
             }
           }
           #if  !defined(FERRET_DISABLE_MULTI_THREADING)
           
             #if defined(FERRET_MEMORY_POOL_SIZE) || defined(FERRET_HARDWARE_ARDUINO)
               mutex memory::allocator::synchronized::lock;
               #undef  FERRET_ALLOCATOR
               #define FERRET_ALLOCATOR memory::allocator::synchronized
             #endif
           
           #endif
           #if !defined(FERRET_RC_POLICY)
           namespace memory {
             namespace gc {
           
           #if !defined(FERRET_RC_TYPE)
             #define FERRET_RC_TYPE unsigned int
           #endif
               
           #if defined(FERRET_DISABLE_RC)
           
           #define FERRET_RC_POLICY memory::gc::no_rc
               
               class no_rc{
               public:
           
                 inline void inc_ref() { }
                 inline bool dec_ref() { return false; }
               };
           
           #else
           
               template<typename T>
               class rc{
               public:
                 rc() : ref_count(0) {}
           
                 inline void inc_ref() { ref_count++; }
                 inline bool dec_ref() { return (--ref_count == 0); }
               
               private:
                 T ref_count;
               };    
           
               #if defined(FERRET_DISABLE_MULTI_THREADING) || !defined(FERRET_STD_LIB)
                 #define FERRET_RC_POLICY memory::gc::rc<FERRET_RC_TYPE>
               #endif
               
               #if defined(FERRET_STD_LIB) && !defined(FERRET_DISABLE_MULTI_THREADING)
                 #define FERRET_RC_POLICY memory::gc::rc<::std::atomic<FERRET_RC_TYPE>>
               #endif
           #endif
             }
           }
           #endif
           template<typename>
           void type_id(){}
           
           using type_id_t = void(*)();
           typedef type_id_t type_t;
           
           class var;
           typedef var const & ref;
           class seekable_i;
           
           template <typename rc>
           class object_i : public rc{
           public:
             object_i() { }
             virtual ~object_i() { };
             
             virtual type_t type() const = 0;
             
           #if !defined(FERRET_DISABLE_STD_OUT)
             virtual void stream_console() const {
               rt::print("var#");
               const void* addr = this;
               rt::print(addr);
             }
           #endif
             
             virtual bool equals(ref) const;
           
             virtual seekable_i* cast_seekable_i() { return nullptr; }
           
             void* operator new(size_t, void* ptr){ return ptr; }
             void  operator delete(void * ptr){ FERRET_ALLOCATOR::free(ptr); }
           };
           
           typedef object_i<FERRET_RC_POLICY> object;
           #if !defined(FERRET_POINTER_T)
             #define FERRET_POINTER_T memory::pointer<object>
           #endif
           
           typedef FERRET_POINTER_T pointer_t;
           class var{
           public:
             explicit inline var(object* o = nullptr) : obj(o) { inc_ref(); }
             inline var(ref o)   : obj(o.obj) { inc_ref(); }
             inline var(var&& o) : obj(o.detach()) { }
               
             ~var() { dec_ref(); }
           
             inline var& operator=(var&& other){
               if (this != &other){
                 dec_ref();
                 obj = other.detach();
               }
               return *this;
             }
             
             inline var& operator= (ref other){
               if (obj != other.obj){
                 dec_ref();
                 obj = other.obj;
                 inc_ref();
               }
               return *this;
             }
           
             bool equals (ref) const;
           
             bool operator==(ref other) const { return equals(other); }
           
             bool operator!=(ref other) const { return !equals(other); }
           
             void* operator new(size_t, void* ptr){ return ptr; }
             
             operator bool() const;
           
           #if !defined(FERRET_DISABLE_STD_OUT)
             void stream_console() const {
               if (obj != nullptr )
                 obj->stream_console();
               else
                 rt::print("nil");
             }
           #endif
                 
             inline object* get() const { return obj; }
             
             template<typename T>
             inline T* cast() const { return static_cast<T*>((object*)obj); }
           
             inline bool is_type(type_t type) const {
               if (obj)
                 return (static_cast<object*>(obj)->type() == type);
               return false;
             }
           
             inline bool is_nil() const { return (obj == nullptr); }
           
           private:
             object* detach(){
               object* _obj = obj;
               obj = nullptr;
               return _obj;
             }
             
             inline void inc_ref(){
           #if !defined(FERRET_DISABLE_RC)
               // Only change if non-null
               if (obj) obj->inc_ref();
           #endif
             }
               
             inline void dec_ref(){
           #if !defined(FERRET_DISABLE_RC)
               // Only change if non-null
               if (obj){
                 // Subtract and test if this was the last pointer.
                 if (obj->dec_ref()){
                   delete obj;
                   obj = nullptr;
                 }
               }
           #endif
             }
               
             pointer_t obj;
           };
           
           template<>
           inline seekable_i* var::cast<seekable_i>() const { return obj != nullptr ? obj->cast_seekable_i() : nullptr; }
           
           template <typename rc>
           bool object_i<rc>::equals(ref o) const {
             return (this == o.get());
           }
           
           #ifdef FERRET_STD_LIB
           std::ostream &operator<<(std::ostream &os, var const &v) {
             v.stream_console();
             return os;
           }
           #endif
           template<typename FT, typename... Args>
           inline var obj(Args... args) {
             void * storage = FERRET_ALLOCATOR::allocate<FT>();
             return var(new(storage) FT(args...));
           }
           
           inline var nil(){
             return var();
           }
           #undef alloca
           
           template<typename T>
           class alloca {
           
             byte memory [sizeof(T)];
             
           public:
             
             template<typename... Args>
             inline explicit alloca(Args... args) {
               (new(memory) T(args...))->inc_ref();
             }
           
             inline operator object*() {
               return (object*)memory;
             }
           };
           
         }

         namespace ferret{
           template <typename T>
           class array {
             size_t  _size{0};
           
           public:
           
             T* data {nullptr};
           
             explicit inline array(size_t s = 0) : _size(s) {
               data = (T *)FERRET_ALLOCATOR::allocate(_size * sizeof(T));
             }
           
             explicit inline array(const T* source, size_t s = 0) : _size(s) {
               data = (T *)FERRET_ALLOCATOR::allocate(_size * sizeof(T));
               for(size_t i = 0; i < _size; i++)
                 data[i] = source[i];
             }
           
           #if defined(FERRET_STD_LIB)
             explicit inline array(std::initializer_list<T> source) :
               _size(source.size()) {
               data = (T *)FERRET_ALLOCATOR::allocate(_size * sizeof(T));
               size_t idx = 0;
               for(auto item : source){  
                 data[idx] = item;
                 idx++;
               }
             }
           #endif
           
             inline array(array&& m) :
               data(m.data), _size(m.size()) { m.data = nullptr; }
           
             inline array(array& m) : _size(m.size()){
               for(size_t i = 0; i < _size; i++)
                 data[i] = m.data[i];
             }
             
             ~array(){
               FERRET_ALLOCATOR::free(data);
             }
           
           
             inline array& operator=(array&& x){
               data = x.data;
               _size = x._size;
               x.data = nullptr;
               return *this;
             }
           
             inline T& operator [](size_t idx)      { return data[idx]; }
             inline T operator [](size_t idx) const { return data[idx]; }
           
             inline T*      begin() const { return &data[0];      }
             inline T*      end()   const { return &data[_size];  }
             inline size_t  size()  const { return _size;         }
           };
           class matrix {
             //row-major
             array<real_t> data;
             //shape
             size_t  rows{0};
             size_t  cols{0};
           
             inline static void into_aux(matrix &){ }
           
             template<typename... Args>
             inline static void into_aux(matrix &m, real_t first, Args... rest){
               m.data[m.data.size() - sizeof...(rest) - 1] = first;
               into_aux(m, rest...);
             }
           
           public:
             inline matrix(size_t r = 0, size_t c = 0) :
               data(r * c), rows(r) , cols(c) { }
           
             template<typename... Args>
             inline matrix(size_t rows, size_t cols, Args... elements)
               : matrix(rows,cols) {
               into_aux(*this, elements...);
             }
           
             inline matrix(matrix&& m) :
               data(m.data), rows(m.rows), cols(m.cols) { }
           
             inline matrix(matrix& m)
               : matrix(m.rows,m.cols){
               for(size_t i = 0; i < data.size(); i++)
                 data[i] = m.data[i];
             }
           
             inline matrix operator+ (const matrix& m) const {
               matrix sum(rows,cols);
               for(size_t i = 0; i < data.size(); i++)
                 sum.data[i] = data[i] + m.data[i];
               return sum;
             }
           
             inline matrix operator- (const matrix& m) const {
               matrix diff(rows,cols);
               for(size_t i = 0; i < data.size(); i++)
                 diff.data[i] = data[i] - m.data[i];
               return diff;
             }
           
             matrix operator* (const matrix& m) const {
               matrix mul = matrix::zeros(rows, m.cols);
           
               if (cols != m.rows)
                 return mul;
           
               for (size_t i = 0; i < rows; i++) {
                 for (size_t j = 0; j < m.cols; j++) {
                   for (size_t k = 0; k < m.rows; k++) {
                     mul(i,j, mul(i,j) + operator()(i,k) * m(k,j));
                   }
                 }
               }
           
               return mul;
             }
           
             matrix operator* (const real_t& val) const {
               matrix mul(rows,cols);
               for(size_t i = 0; i < data.size(); i++)
                 mul.data[i] = data[i] * val;
               return mul;
             }
           
             inline real_t operator()(size_t row, size_t col) const {
               return data[row * cols + col];
             }
           
             inline void operator()(size_t row, size_t col, real_t val) {
               data[row * cols + col] = val;
             }
           
             inline matrix& operator=(matrix&& x){
               data = array<real_t>(x.data);
               rows = x.rows;
               cols = x.cols;
               return *this;
             }
           
             inline bool operator ==(const matrix& m) const {
               for (size_t i = 0; i < data.size(); i++)
                 if (data[i] != m.data[i])
                   return false;
               return true;
             }
           
           #if defined(FERRET_STD_LIB)
             friend std::ostream& operator<< (std::ostream& stream, const matrix& x) {
               stream << "[";
               for (size_t r = 0; r < x.rows; r++){
                 stream << "[";
                 stream << x(r, 0);
                 for (size_t c = 1; c < x.cols; c++)
                   stream << " " << x(r,c);
                 stream << "]";
               }
               return stream << "]";
             }
           #endif
           
             inline static matrix empty(size_t r = 0, size_t c = 0) {
               return matrix(r,c);
             }
           
             inline static void fill(matrix& m, real_t val) {
               for(size_t i = 0; i < m.data.size(); i++)
                 m.data[i] = val;
             }
             
             inline static matrix zeros(size_t r = 0, size_t c = 0) {
               matrix m(r,c);
               fill(m, real_t(0));
               return m;
             }
           
             inline static matrix ones(size_t r = 0, size_t c = 0) {
               matrix m(r,c);
               fill(m, real_t(1));
               return m;
             }
           
             inline static matrix full(size_t r = 0, size_t c = 0, real_t v = real_t(0)) {
               matrix m(r,c);
               fill(m, v);
               return m;
             }
           
             static matrix eye(size_t n = 1){
               matrix m = matrix::zeros(n,n);
           
               for(size_t r = 0; r < m.rows; r++)
                 m(r,r,real_t(1));
           
               return m;
             }
           
             template<size_t rows, size_t cols, typename... Args>
             inline static matrix into(Args... rest){
               matrix m(rows, cols);
               into_aux(m, rest...);
               return m;
             }
           
             inline static size_t row_count(const matrix& m){
               return m.rows;
             }
           
             inline static size_t column_count(const matrix& m){
               return m.cols;
             }
           
             static real_t norm_euclidean(const matrix& m){
               real_t norm = real_t(0);
           
               for(size_t i = 0; i < m.data.size(); i++){
                 norm += m.data[i] * m.data[i];
               }
           
               return real_t(sqrt((double)norm));
             }
           
             static matrix normalise(const matrix& m){
               real_t mag = matrix::norm_euclidean(m);
               matrix norm = matrix::zeros(m.rows,m.cols);
           
               if (mag == real_t(0))
                 return norm;
           
               for(size_t i = 0; i < m.data.size(); i++)
                 norm.data[i] = m.data[i] / mag;
           
               return norm;
             }
           };
         }

         // Runtime Prototypes
         namespace ferret{
             namespace runtime {
               var list(ref v);
               template <typename... Args>
               var list(ref first, Args const & ... args);
           
               inline bool is_seqable(ref seq);
           
               inline var first(ref seq);
               inline var rest(ref seq);
               inline var cons(ref x, ref seq);
           
               var nth(var seq, number_t index);
               var nthrest(var seq, number_t index);
           
               inline size_t count(ref seq);
           
               inline var range(number_t low, number_t high);
             }
           
           #define for_each(x,xs) for(var _tail_ = rt::rest(xs), x = rt::first(xs);     \
                                      !_tail_.is_nil();                                 \
                                      x = rt::first(_tail_), _tail_ = rt::rest(_tail_))
           template<typename T, typename... Args>
           inline var run(T const & fn, Args const & ... args);
                 
           template<typename T>
           inline var run(T const & fn);
           
           template<>
           inline var run(ref);
           
           namespace runtime{
             inline var apply(ref fn, ref argv);
           }
         }
        #endif

        // Objects
        namespace ferret{
         #ifndef FERRET_OBJECT_SEEKABLE_I
               #define FERRET_OBJECT_SEEKABLE_I

                class seekable_i {
                  public:

                    virtual var cons(ref x) = 0;
                    virtual var first() = 0;
                    virtual var rest() = 0;

                #if !defined(FERRET_DISABLE_STD_OUT)
                    static void stream_console(ref coll) {
                      var tail = rt::rest(coll);

                      rt::print('(');
                      if (tail)
                        rt::first(coll).stream_console();

                      for_each(i, tail){
                        rt::print(' ');
                        i.stream_console();
                      }
                      rt::print(')');
                    }
                #endif

                    static bool equals(var lhs, var rhs) {

                      for(;;lhs = rt::rest(lhs), rhs = rt::rest(rhs)){

                        ref lf = rt::first(lhs);
                        ref rf = rt::first(rhs);

                        if (lf.is_nil() && rf.is_nil())
                          return true;
                        
                        if (lf != rf)
                          return false;
                      }
                    }
                  };
               #endif
         #ifndef FERRET_OBJECT_LAMBDA_I
               #define FERRET_OBJECT_LAMBDA_I

                struct lambda_i : public object {
                  virtual var invoke(ref args) const = 0;
                  type_t type() const { return type_id<lambda_i>; }
                };
               #endif
         #ifndef FERRET_OBJECT_DEREF_I
               #define FERRET_OBJECT_DEREF_I

                class deref_i : public object {
                 public:

                  virtual var deref() = 0;
                };
               #endif
         #ifndef FERRET_OBJECT_BOOLEAN
               #define FERRET_OBJECT_BOOLEAN

                class boolean final : public object {
                  const bool value;
                public:

                  type_t type() const final { return type_id<boolean>; }

                  bool equals(ref o) const final {
                    return (value == o.cast<boolean>()->container());
                  }

                #if !defined(FERRET_DISABLE_STD_OUT)
                  void stream_console() const final {
                    if (value)
                      rt::print("true");
                    else
                      rt::print("false");
                  }
                #endif

                  explicit boolean(bool b) : value(b) {} 

                  bool container() const {
                    return value;
                  }
                };

                namespace cached{
                  const var true_o = obj<::ferret::boolean>(true);
                  const var false_o = obj<::ferret::boolean>(false);
                }

                var::operator bool() const {
                  if (obj == nullptr)
                    return false;
                  else if (obj->type() == (type_t)type_id<boolean>)
                    return cast<boolean>()->container();
                  else
                    return true;
                }

                bool var::equals (ref other) const {
                  if (get() == other.get())
                    return true;

                  if (!is_nil() && !other.is_nil()){

                    if (rt::is_seqable(*this) && rt::is_seqable(other))
                      return seekable_i::equals(*this, other);
                    else if (obj->type() != other.get()->type())
                      return false;
                    else
                      return get()->equals(other);
                  
                  }else
                    return false;
                }
               #endif
         #ifndef FERRET_OBJECT_POINTER
               #define FERRET_OBJECT_POINTER

                class pointer final : public object {
                  void * payload;
                public:


                  type_t type() const final { return type_id<pointer>; }

                  bool equals(ref o) const final {
                    return (payload == o.cast<pointer>()->payload);
                  }

                  explicit pointer(void* p) : payload(p) {}
                  
                  template<typename T> static T* to_pointer(ref v){
                    return ((T *) v.cast<pointer>()->payload);
                  }
                  template<typename T> static T& to_reference(ref v){
                    return (*(pointer::to_pointer<T>(v)));
                  }
                };
               #endif
         #ifndef FERRET_OBJECT_VALUE
               #define FERRET_OBJECT_VALUE

                template <typename T>
                class value final : public object {
                  T payload;
                public:

                  type_t type() const final { return type_id<value>; }

                  template <typename... Args>
                  explicit value(Args&&... args) : payload(static_cast<Args&&>(args)...) { } 

                  T to_value() const {
                    return payload;
                  }

                  static T to_value(ref v){
                    return v.cast<value<T>>()->payload;
                  }
                  
                  T & to_reference() {
                    return payload;
                  }
                    
                  static T & to_reference(ref v) {
                    return v.cast<value<T>>()->to_reference();
                  }  
                };

                typedef value<matrix> matrix_t;
               #endif
         #ifndef FERRET_OBJECT_NUMBER
               #define FERRET_OBJECT_NUMBER

                class number final : public object {
                  const real_t n;
                public:

                  type_t type() const final { return type_id<number>; }

                  bool equals(ref o) const final {
                    return (rt::abs(n - number::to<real_t>(o)) < real_epsilon);
                  }

                #if !defined(FERRET_DISABLE_STD_OUT)
                  void stream_console() const final {
                    rt::print(n);
                  }
                #endif

                  template<typename T> explicit number(T x) : n(real_t(x)) {} 

                  template<typename T> static T to(ref v){
                    return (T)v.cast<number>()->n;
                  }
                };
               #endif
         #ifndef FERRET_OBJECT_EMPTY_SEQUENCE
               #define FERRET_OBJECT_EMPTY_SEQUENCE

                class empty_sequence final : public object {

                  type_t type() const final { return type_id<empty_sequence>; }

                #if !defined(FERRET_DISABLE_STD_OUT)
                  void stream_console() const final {
                    rt::print("()");
                  }
                #endif
                };

                namespace cached{
                  const var empty_sequence_o = obj<::ferret::empty_sequence>();
                }
               #endif
         #ifndef FERRET_OBJECT_SEQUENCE
               #define FERRET_OBJECT_SEQUENCE

                class sequence final : public object, public seekable_i {
                    const var next;
                    const var data;
                  public:

                    type_t type() const final { return type_id<sequence>; }

                #if !defined(FERRET_DISABLE_STD_OUT)
                    void stream_console() const final {
                      seekable_i::stream_console(var((object*)this));
                    }
                #endif

                    explicit sequence(ref d = nil(), ref n = nil()) : next(n), data(d) {} 

                    virtual seekable_i* cast_seekable_i() { return this; }

                    var cons(ref x) final {
                      return obj<sequence>(x, var(this));
                    }

                    var first() final {
                      return data;
                    }

                    var rest() final {
                      return next;
                    }

                    template <typename T>
                    static T to(ref){
                      T::unimplemented_function;
                    }

                    template <typename T>
                    static var from(T){
                      T::unimplemented_function; return nil();
                    }

                  };

                  namespace runtime {
                    inline var list() { 
                      return cached::empty_sequence_o;
                    }
                    inline var list(ref v) { 
                      return obj<sequence>(v,cached::empty_sequence_o);
                    }
                                      
                    template <typename... Args>
                    inline var list(ref first, Args const & ... args) { 
                      return obj<sequence>(first, list(args...));
                    }
                  }

                  #ifdef FERRET_STD_LIB
                  typedef ::std::vector<var>  std_vector;

                  template <> std_vector sequence::to(ref v) { 
                    std_vector ret;
                    for_each(i, v)
                      ret.push_back(i);
                    return ret;
                  }

                  template <> var sequence::from(std_vector v) { 
                    var ret;
                    std::vector<var>::reverse_iterator rit;
                    // cppcheck-suppress postfixOperator
                    for(rit = v.rbegin(); rit != v.rend(); rit++)
                      ret = rt::cons(*rit,ret);
                    return ret;
                  }
                  #endif
               #endif
         #ifndef FERRET_OBJECT_LAZY_SEQUENCE
               #define FERRET_OBJECT_LAZY_SEQUENCE

                class lazy_sequence final : public object, public seekable_i {
                  mutex lock;
                  bool cache;
                  var thunk;
                  var data;
                  var seq;

                  void yield(){
                    if (thunk.is_nil())
                      return;

                    seq = run(thunk);

                    if (data.is_nil()){
                      data = rt::first(seq);
                      seq = rt::rest(seq);
                    }

                    thunk = nil();
                  }

                public:

                  type_t type() const final { return type_id<lazy_sequence>; }

                #if !defined(FERRET_DISABLE_STD_OUT)
                  void stream_console() const final {
                    seekable_i::stream_console(var((object*)this));
                  }
                #endif

                  explicit lazy_sequence(ref t, bool c = false) : cache(c), thunk(t) {}
                  explicit lazy_sequence(ref d, ref t, bool c = false) : cache(c), thunk(t), data(d) {}

                  virtual seekable_i* cast_seekable_i() { return this; }

                  var cons(ref x) final {
                    lock_guard guard(lock);

                    if (data.is_nil())
                      return obj<lazy_sequence>(x, thunk, cache);

                    return obj<sequence>(x, var((object*)this));
                  }

                  var first() final {
                    lock_guard guard(lock);
                    if (cache)
                      yield();
                    else
                      if (data.is_nil())
                        return rt::first(run(thunk));
                    return data;
                  }
                    
                  var rest() final {
                    lock_guard guard(lock);
                    var tail;

                    if (cache){
                      yield();
                      tail = seq;
                    }else{
                      tail = run(thunk);
                      if (data.is_nil())
                        return rt::rest(tail);
                    }

                    if (tail.is_nil())
                      return rt::list();

                    return tail;
                  }

                  static var from(ref seq) {
                    class walk : public lambda_i {
                      var seq;
                    public:
                      explicit walk(ref s) : seq(s) { }
                      var invoke(ref) const final {
                        var tail = rt::rest(seq);
                        if (tail.is_nil())
                          return nil();

                        return obj<lazy_sequence>(rt::first(seq), obj<walk>(tail), true);
                      }
                    };

                    return obj<lazy_sequence>(obj<walk>(seq), true);
                  }
                };
               #endif
         #ifndef FERRET_OBJECT_ARRAY_SEQUENCE
               #define FERRET_OBJECT_ARRAY_SEQUENCE

                template<typename element_t, typename object_t>
                class array_seq : public object , public seekable_i {
                  size_t pos;

                public:
                  typedef array<element_t> array_t;
                  typedef value<array_t> value_t;

                  var storage;

                  explicit array_seq(const element_t* src, size_t s = 0)
                    : pos (0), storage(obj<value_t>(src, s)) { }

                  explicit array_seq(var b, size_t p = 0) : pos(p), storage(b){ }

                  explicit array_seq(size_t size) : pos(0), storage(obj<value_t>(size)){ }

                  type_t type() const final { return type_id<array_seq>; }

                #if !defined(FERRET_DISABLE_STD_OUT)
                    void stream_console() const final {
                      seekable_i::stream_console(var((object*)this));
                    }
                #endif

                  virtual seekable_i* cast_seekable_i() { return this; }
                  
                  var cons(ref x) final {
                    return obj<sequence>(x, var(this));
                  }

                  var first() final {
                    array_t& b = value_t::to_reference(storage);
                    return obj<object_t>(b[pos]);
                  }

                  var rest() final {
                    array_t& b = value_t::to_reference(storage);

                    if (pos < b.size() - 1)
                      return obj<array_seq>(storage, pos + 1);

                    return rt::list();
                  }
                };

                template <>
                class array<var> {
                  size_t  _size{0};

                  var* allocate(){
                    var* storage = static_cast<var*>(FERRET_ALLOCATOR::allocate(_size * sizeof(var))) ;
                    for(size_t i = 0; i < _size; i++)
                      new (&storage[i]) var();
                    return storage;
                  }

                public:

                  var* data {nullptr};

                  explicit inline array(size_t s = 0) : _size(s), data(allocate()) { }

                  inline array(array&& m) :
                    _size(m.size()), data(m.data) { m.data = nullptr; }

                  inline array(array& m) : _size(m.size()), data(allocate()) {
                    for(size_t i = 0; i < _size; i++)
                      data[i] = m.data[i];
                  }

                  ~array(){
                    for(size_t i = 0; i < size(); i++)
                      (&data[i])->~var();
                    FERRET_ALLOCATOR::free(data);
                  }

                  inline array& operator=(array&& x){
                    data = x.data;
                    _size = x._size;
                    x.data = nullptr;
                    return *this;
                  }

                  inline var& operator [](size_t idx)      { return data[idx]; }
                  inline var operator [](size_t idx) const { return data[idx]; }

                  inline var*    begin() const { return &data[0];      }
                  inline var*    end()   const { return &data[_size];  }
                  inline size_t  size()  const { return _size;         }
                };

                typedef array<var> var_array_t;
                typedef value<var_array_t> var_array;
                typedef array_seq<var,var> var_array_seq;

                template<>
                class array_seq<var,var> : public object , public seekable_i {
                  size_t pos{0};

                  inline static void into_aux(ref){ }

                  template<typename... Args>
                  inline static void into_aux(ref arr, ref first, Args... rest){
                    auto& data = var_array::to_reference(arr);
                    data[data.size() - sizeof...(rest) - 1] = first;
                    into_aux(arr, rest...);
                  }

                public:
                  var storage;

                  explicit array_seq(var b, size_t p = 0) : pos(p), storage(b){ }

                  type_t type() const final { return type_id<array_seq>; }

                #if !defined(FERRET_DISABLE_STD_OUT)
                    void stream_console() const final {
                      seekable_i::stream_console(var((object*)this));
                    }
                #endif

                  virtual seekable_i* cast_seekable_i() { return this; }

                  var cons(ref x) final {
                    return obj<sequence>(x, var(this));
                  }

                  var first() final {
                    var_array_t& b = var_array::to_reference(storage);
                    return b[pos];
                  }

                  var rest() final {
                    var_array_t& b = var_array::to_reference(storage);

                    if (pos < b.size() - 1)
                      return obj<array_seq>(storage, pos + 1);

                    return rt::list();
                  }

                  template<typename... Args>
                  static inline var into(Args... rest){
                    var arr = obj<var_array>(sizeof...(rest));
                    into_aux(arr, rest...);
                    return obj<var_array_seq>(arr);
                  }
                };

                namespace runtime{
                  template<typename... Args>
                  static inline var dense_list(Args... rest){
                    return var_array_seq::into(rest...);
                  }
                }
               #endif
         #ifndef FERRET_OBJECT_D_LIST
               #define FERRET_OBJECT_D_LIST

                class d_list final : public lambda_i, public seekable_i {

                  var data;

                  var dissoc_aux(ref k) const {
                    ref _keys = rt::first(data);
                    var _values = rt::rest(data);

                    var new_keys;
                    var new_values;
                    
                    for_each(i, _keys){
                      if ( i == k)
                        continue;
                      new_keys = rt::cons(i, new_keys);
                      new_values = rt::cons(rt::first(_values), new_values);
                      _values = rt::rest(_values);
                    }
                      
                    return rt::cons(new_keys,new_values);
                  }
                  
                 public:

                  type_t type() const final { return type_id<d_list>; }

                #if !defined(FERRET_DISABLE_STD_OUT)
                  void stream_console() const final {
                    data.stream_console();
                  }
                #endif

                  explicit d_list() : data(rt::list(rt::list())) { }
                  explicit d_list(ref l) : data(l) { }
                  
                  var assoc(ref k, ref v) const {
                    ref map = dissoc_aux(k);
                    ref _keys = rt::first(map);
                    ref _values = rt::rest(map);

                    return obj<d_list>(rt::cons(rt::cons(k,_keys),
                                                     rt::cons(v,_values)));
                  }

                  var dissoc(ref k) const {
                    return obj<d_list>(dissoc_aux(k));
                  }
                  
                  var val_at(ref args) const {
                    ref key = rt::first(args);
                    ref not_found = rt::first(rt::rest(args));

                    ref _keys = rt::first(data);
                    var _values = rt::rest(data);

                    for_each(i, _keys){
                      if (key == i)
                        return rt::first(_values);

                      _values = rt::rest(_values);
                    }
                    
                    if (!not_found.is_nil()){
                      return not_found;
                    }else{
                      return nil();  
                    }
                  }

                  var invoke(ref args) const final {
                    return val_at(args);
                  }

                  var vals () const { return rt::rest(data);}
                  var keys () const { return rt::first(data);}

                  virtual seekable_i* cast_seekable_i() { return this; }
                  
                  var cons(ref v) final {
                    return rt::list(v,data);
                  }
                  
                  var first() final {
                    ref _keys = rt::first(data);
                    ref _values = rt::rest(data);
                    return rt::list(rt::first(_keys),rt::first(_values));
                  }
                  
                  var rest() final {
                    ref _keys = rt::first(data);
                    ref _values = rt::rest(data);

                    if(rt::rest(_keys).is_type(type_id<empty_sequence>))
                      return rt::list();
                    
                    return obj<d_list>(rt::cons(rt::rest(_keys),rt::rest(_values)));
                  }
                };

                template<>
                inline var obj<d_list>(var keys, var vals) {
                  void * storage = FERRET_ALLOCATOR::allocate<d_list>();
                  return var(new(storage) d_list(rt::cons(keys,vals)));
                }

                #if !defined(FERRET_MAP_TYPE)
                typedef d_list map_t;
                #endif
               #endif
         #ifndef FERRET_OBJECT_KEYWORD
               #define FERRET_OBJECT_KEYWORD

                class keyword final : public lambda_i {
                  const number_t hash;

                  static constexpr number_t hash_key(const char * key){
                    return *key ? (number_t)*key + hash_key(key + 1) : 0;
                  }
                  
                public:

                  type_t type() const final { return type_id<keyword>; }

                  bool equals(ref o) const final { return (hash == o.cast<keyword>()->hash); }

                #if !defined(FERRET_DISABLE_STD_OUT)
                  void stream_console() const final {
                    rt::print("keyword#");
                    rt::print(hash);
                  }
                #endif

                  explicit keyword(number_t w) : hash(w) {} 
                  explicit keyword(const char * str): hash(hash_key(str)) { }

                  var invoke(ref args) const final {
                    ref map = rt::first(args);
                    ref map_args = rt::cons(var((object*)this), rt::rest(args));

                    if (map.is_type(type_id<map_t>)){
                      return map.cast<map_t>()->val_at(map_args);
                    }

                    return nil();
                  }
                };
               #endif
         #ifndef FERRET_OBJECT_STRING
               #define FERRET_OBJECT_STRING

                class string final : public object, public seekable_i {
                  var data;

                  typedef array_seq<byte, number> array_seq_t;
                  typedef array<byte> array_t;

                  void from_char_pointer(const char * str, int length){
                    data = obj<array_seq_t>((byte*)str, (size_t)(length + 1));

                    var seq = (data.cast<array_seq_t>()->storage);
                    auto & arr = value<array_t>::to_reference(seq).data;
                    arr[length] = 0x00;
                  }

                public:

                  type_t type() const final { return type_id<string>; }

                #if !defined(FERRET_DISABLE_STD_OUT)
                  void stream_console() const final {
                    var packed = string::pack(var((object*)this));
                    char* str = string::c_str(packed);
                    rt::print(str);
                  }
                #endif

                  explicit string() : data(rt::list()) {} 

                  explicit string(ref s) : data(s) {}

                  explicit string(const char * str) {
                    int length = 0;
                    for (length = 0; str[length] != 0x00; ++length);
                    from_char_pointer(str, length);
                  }

                  explicit string(const char * str,number_t length) { from_char_pointer(str,length); }

                  virtual seekable_i* cast_seekable_i() { return this; }

                  var cons(ref x) final {
                    return obj<string>(rt::cons(x,data));
                  }

                  var first() final {
                    return rt::first(data);
                  }

                  var rest() final {
                    ref r = rt::rest(data);

                    if (r.is_type(type_id<array_seq_t>))
                      if (rt::first(r) == obj<number>(0))
                        return rt::list();

                    if (!r.is_type(type_id<empty_sequence>))
                      return obj<string>(r);

                    return rt::list();
                  }

                  static var pack(ref s)  {
                    if (s.cast<string>()->data.is_type(type_id<array_seq_t>))
                      return s.cast<string>()->data;

                    size_t size = rt::count(s);
                    var packed_array = obj<value<array_t>>(size + 1);
                    auto& packed_data = value<array_t>::to_reference(packed_array).data;

                    size_t pos = 0;
                    for_each(c, s){
                      packed_data[pos] = number::to<byte>(c);
                      pos++;
                    }
                    packed_data[pos] = 0x00;

                    return obj<array_seq_t>(packed_array);
                  }

                  static char* c_str(ref s)  {
                    var seq = (s.cast<array_seq_t>()->storage);
                    auto & str = value<array<byte>>::to_reference(seq).data;
                    return (char*) str;
                  }

                  template <typename T>
                  static T to(ref){
                    T::unimplemented_function;
                  }
                };

                #ifdef FERRET_STD_LIB
                template<>
                inline var obj<string>(std::string s) {
                  void * storage = FERRET_ALLOCATOR::allocate<string>();
                  return var(new(storage) string(s.c_str(), (number_t)s.size()));
                }

                template <> ::std::string string::to(ref str) {
                  var packed = string::pack(str);
                  return std::string(string::c_str(packed));
                }
                #endif

                #ifdef FERRET_HARDWARE_ARDUINO
                template<>
                inline var obj<string>(String s) {
                  void * storage = FERRET_ALLOCATOR::allocate<string>();
                  return var(new(storage) string(s.c_str(), (number_t)s.length()));
                }

                template <> String string::to(ref str) {
                  var packed = string::pack(str);
                  return String(string::c_str(packed));
                }
                #endif
               #endif
         #ifndef FERRET_OBJECT_ATOMIC
               #define FERRET_OBJECT_ATOMIC

                class atomic final : public deref_i {
                  mutex lock;
                  var data;

                  public:

                  type_t type() const final { return type_id<atomic>; }

                #if !defined(FERRET_DISABLE_STD_OUT)
                  void stream_console() const final {
                    rt::print("atom<");
                    data.stream_console();
                    rt::print('>');
                  }
                #endif

                  explicit atomic(ref d) : data(d) {} 

                  var swap(ref f, ref args){
                    lock_guard guard(lock);
                    data = f.cast<lambda_i>()->invoke(rt::cons(data, args));
                    return data;
                  }

                  var reset(ref newval){
                    lock_guard guard(lock);
                    data = newval;
                    return data;
                  }
                  
                  var deref() final {
                    lock_guard guard(lock);
                    return data;
                  }
                };
               #endif
         #ifndef FERRET_OBJECT_ASYNC
               #define FERRET_OBJECT_ASYNC

                #ifdef FERRET_STD_LIB
                class async final : public deref_i {
                  mutex lock;
                  bool cached;
                  var value;
                  var fn;
                  std::future<var> task;

                  inline var exec() {
                    return run(fn);
                  }
                  
                  public:

                  explicit async(ref f) :
                    cached(false), value(nil()), fn(f), 
                    task(std::async(std::launch::async, [this](){ return exec(); })){ }

                  type_t type() const final { return type_id<async>; }

                #if !defined(FERRET_DISABLE_STD_OUT)
                  void stream_console() const final {
                    rt::print("future<");
                    fn.stream_console();
                    rt::print('>');
                  }
                #endif

                  bool is_ready(){
                    lock_guard guard(lock);
                    if (cached)
                      return true;
                    return task.wait_for(std::chrono::seconds(0)) == std::future_status::ready;
                  }

                  void get(){
                    if (!cached){
                      value = task.get();
                      cached = true;
                    }
                  }

                  var deref() final {
                    lock_guard guard(lock);
                    get();
                    return value;
                  }
                };
                #endif
               #endif
         #ifndef FERRET_OBJECT_DELAYED
               #define FERRET_OBJECT_DELAYED

                class delayed final : public deref_i {
                  mutex lock;
                  var fn;
                  var val;
                  
                  public:

                  type_t type() const final { return type_id<delayed>; }

                  explicit delayed(ref f) : fn(f) {} 
                  
                  var deref() final {
                    lock_guard guard(lock);
                    if (!fn.is_nil()){
                      val = fn.cast<lambda_i>()->invoke(nil());
                      fn = nil();
                    }
                    return val;
                  }
                };
               #endif
         #ifndef FERRET_OBJECT_ELAPSED_MICROS
               #define FERRET_OBJECT_ELAPSED_MICROS

                #if !defined(FERRET_SAFE_MODE)
                class elapsed_micros : public object {
                  mutex lock;
                  unsigned long us;

                #if defined(FERRET_HARDWARE_ARDUINO)
                  inline unsigned long now() const{
                    return ::micros();
                  }
                #elif defined(FERRET_STD_LIB)
                  inline unsigned long now() const{
                    auto now = ::std::chrono::high_resolution_clock::now();
                    auto epoch = now.time_since_epoch();
                    return (unsigned long)::std::chrono::duration_cast<::std::chrono::microseconds>(epoch).count();
                  }
                #endif
                  
                 public:

                  elapsed_micros(void) { us = now(); }

                  void reset() {
                    lock_guard guard(lock);
                    us = now();
                  }
                  
                  type_t type() const { return type_id<elapsed_micros>; }

                #if !defined(FERRET_DISABLE_STD_OUT)
                  void stream_console() const {
                    rt::print("micros#");
                    rt::print(elapsed());
                  }
                #endif

                  inline real_t elapsed() const { return (real_t)(now() - us); }
                  inline bool is_elapsed(real_t t) const { return (elapsed() >= t); }
                };
                #endif
               #endif
         #ifndef FERRET_OBJECT_FN_THROTTLER_O
               #define FERRET_OBJECT_FN_THROTTLER_O

                #if !defined(FERRET_SAFE_MODE)
                class fn_throttler : public lambda_i {
                  var fn;
                  elapsed_micros timer;
                  real_t rate;
                  bool blocking;

                #if defined(FERRET_HARDWARE_ARDUINO)
                  inline void _wait(real_t t) const{
                    ::delayMicroseconds((number_t)t);
                  }
                #elif defined(FERRET_STD_LIB)
                  inline void _wait(real_t t) const{
                    auto duration = ::std::chrono::microseconds((number_t)t);
                    ::std::this_thread::sleep_for(duration);
                  }
                #endif
                  
                  var exec(ref args){
                    if (blocking)
                      _wait(rate - timer.elapsed());

                    if (timer.is_elapsed(rate)){
                      timer.reset();
                      return rt::apply(fn, args);
                    }
                    
                    return nil();
                  }
                  
                public:

                  explicit fn_throttler(var f, real_t r, bool b) : fn(f), rate(r), blocking (b) { }

                  var invoke(ref args) const final {
                    return var((object*)this).cast<fn_throttler>()->exec(args);
                  }
                };
                #endif
               #endif
         #ifndef FERRET_OBJECT_FSM
               #define FERRET_OBJECT_FSM

                class fsm final : public lambda_i {
                  mutex lock;
                  var state;
                  var transitions;
                public:

                  inline fsm(ref s, ref t) : state(s), transitions(t){ }

                  inline var invoke(ref) const final {
                    return var((object*)this).cast<fsm>()->yield();
                  }

                  var yield() {
                    lock_guard guard(lock);
                    var value;

                    if (state.is_type(type_id<lambda_i>))
                      value = run(state);
                    else
                      value = state;

                    var next = transitions.cast<lambda_i>()->invoke(rt::list(state));

                    if (next.is_nil())
                      next = state;

                    state = next;
                    return value;
                  }
                    
                };
               #endif
         #ifndef FERRET_OBJECT_PID_CONTROLLER
               #define FERRET_OBJECT_PID_CONTROLLER

                template <typename T>
                class pid_controller : public lambda_i {
                  mutex lock;
                  mutable T setpoint;
                  mutable T prev_error;
                  mutable T total_error;
                  mutable T error;
                  mutable T result;
                  mutable T input;

                  T p;
                  T i;
                  T d;
                  T maximum_output;
                  T minimum_output;
                  T maximum_input;
                  T minimum_input;
                  bool continuous;
                  var setpoint_fn;

                  void set_setpoint(ref p) {
                    lock_guard guard(lock);
                    T sp = number::to<T>(p);
                    if (maximum_input > minimum_input) {
                      if (sp > maximum_input) {
                        setpoint = maximum_input;
                      } else if (sp < minimum_input) {
                        setpoint = minimum_input;
                      } else {
                        setpoint = sp;
                      }
                    } else {
                      setpoint = sp;
                    }
                  }
                  
                  var step(ref in) {
                    lock_guard guard(lock);
                    input = number::to<T>(in);

                    // Calculate the error signal
                    error = setpoint - input;

                    // If continuous is set to true allow wrap around
                    if (continuous) {
                      if (rt::abs(error) > ((maximum_input - minimum_input) / real_t(2))) {
                        if (error > real_t(0)) {
                          error = (error - maximum_input) + minimum_input;
                        } else {
                          error = (error + maximum_input) - minimum_input;
                        }
                      }
                    }
                                              
                    /*
                     * Integrate the errors as long as the upcoming integrator does
                     * not exceed the minimum and maximum output thresholds
                     */
                    if ((((total_error + error) * i) < maximum_output) &&
                        (((total_error + error) * i) > minimum_output)) {
                      total_error += error;
                    }
                                              
                    // Perform the primary PID calculation
                    result = ((p * error) + (i * total_error) + (d * (error - prev_error)));
                                              
                    // Set the current error to the previous error for the next cycle
                    prev_error = error;
                                              
                    // Make sure the final result is within bounds
                    if (result > maximum_output) {
                      result = maximum_output;
                    } else if (result < minimum_output) {
                      result = minimum_output;
                    }

                    return obj<number>(result);
                  }
                  
                public:
                  pid_controller(T kp, T ki, T kd,
                                 T inMin, T inMax, T outMin, T outMax,
                                 bool cont,
                                 ref sp):
                    p(kp),
                    i(ki),
                    d(kd),
                    maximum_output(outMax),
                    minimum_output(outMin),
                    maximum_input(inMax),
                    minimum_input(inMin),
                    continuous(cont){

                    if (sp.is_type(type_id<lambda_i>)){
                      setpoint_fn = sp;
                      set_setpoint(run(setpoint_fn));
                    }else{
                      set_setpoint(sp);
                    }

                    prev_error = 0;
                    total_error = 0;
                    error = 0;
                    result = 0;
                    input = 0;
                  }

                  var invoke(ref args) const final {
                    if (!setpoint_fn.is_nil())
                      var((object*)this).cast<pid_controller<T>>()
                        ->set_setpoint(run(setpoint_fn));
                    
                    return var((object*)this).cast<pid_controller<T>>()
                      ->step(rt::first(args));
                  }
                };
               #endif
         #ifndef FERRET_OBJECT_MOVING_AVERAGE_FILTER
               #define FERRET_OBJECT_MOVING_AVERAGE_FILTER

                template <typename T>
                class moving_average_filter : public lambda_i {
                  mutex lock;
                  T alpha;
                  mutable T avrg;

                  var step(T data) {
                    lock_guard guard(lock);
                    avrg = ((alpha * data) + ((1. - alpha) * avrg));
                    return obj<number>(avrg);
                  }
                  
                public:

                  explicit moving_average_filter(T a) : alpha(a), avrg(0) { }

                  var invoke(ref args) const final {
                    return var((object*)this).cast<moving_average_filter<T>>()
                      ->step(number::to<T>(rt::first(args)));
                  }
                };
               #endif
        }

        // Symbols
        namespace c_interop{
         using namespace ferret;

         #if defined(ARDUINO)
           typedef ferret::boolean boolean;
         #endif

         var pc1;
         var pf1;
        }


        // Runtime Implementations
        #ifndef FERRET_RUNTIME_CPP
        #define FERRET_RUNTIME_CPP

         namespace ferret{
           namespace runtime {
             inline bool is_seqable(ref coll){
               if(coll.cast<seekable_i>())
                 return true;
               else
                 return false;
             }
           
             inline var first(ref seq){
               if (seq.is_nil() || seq.is_type(type_id<empty_sequence>))
                 return nil();
               return seq.cast<seekable_i>()->first();
             }
           
             inline var rest(ref seq){
               if (seq.is_nil() || seq.is_type(type_id<empty_sequence>))
                 return nil();
               return seq.cast<seekable_i>()->rest();
             }
           
             inline var cons(ref x, ref seq){
               if (seq.is_nil() || seq.is_type(type_id<empty_sequence>))
                 return rt::list(x);
               return seq.cast<seekable_i>()->cons(x);
             }
           
             var nth(var seq, number_t index){
               if (index < 0)
                 return nil();
           
               for(number_t i = 0; i < index; i++)
                 seq = rt::rest(seq);
               return rt::first(seq);
             }
           
             var nthrest(var seq, number_t index){
               for(number_t i = 0; i < index; i++)
                 seq = rt::rest(seq);
           
               if (seq.is_nil())
                 return rt::list(); 
           
               return seq;
             }
             
             inline size_t count(ref seq){
               size_t acc = 0;
           
               for(var tail = rt::rest(seq);
                   !tail.is_nil();
                   tail = rt::rest(tail))
                 acc++;
           
               return acc;
             }
           
             inline var range(number_t low, number_t high){
               class seq : public lambda_i {
                 number_t low, high;
               public:
                 explicit seq(number_t l, number_t h) : low(l), high(h) { }
                 var invoke(ref) const final {
                   if (low < high)
                     return obj<lazy_sequence>(obj<number>(low), obj<seq>((low + 1), high));
                   return nil();
                 }
               };
               return obj<lazy_sequence>(obj<seq>(low, high));
             }
           }
           template<typename T, typename... Args>
           inline var run(T const & fn, Args const & ... args) {
             return fn.invoke(rt::list(args...));
           }
           
           template<typename T>
           inline var run(T const & fn) {
             return fn.invoke(nil());
           }
           
           template<>
           inline var run(ref fn) {
             return fn.cast<lambda_i>()->invoke(nil());
           }
           
           template<typename... Args>
           inline var run(ref fn, Args const & ... args) {
             return fn.cast<lambda_i>()->invoke(rt::list(args...));
           }
           
           namespace runtime {
             inline var apply(ref f, ref argv){
               if (rt::rest(argv).is_type(type_id<empty_sequence>))
                 return f.cast<lambda_i>()->invoke(rt::first(argv));
           
               struct{
                 var operator()(ref seq) const {
                   ref head = rt::first(seq);
           
                   if (head.is_nil())
                     return cached::empty_sequence_o;
           
                   if (head.cast<seekable_i>())
                     return head;
           
                   return rt::cons(head, (*this)(rt::rest(seq)));
                 }
               } spread;
               
               return f.cast<lambda_i>()->invoke(spread(argv));
             }
           }
         }
        #endif

        // Lambda Prototypes
        namespace c_interop{
                 class FN__2037  {
                public:

                  var invoke (ref _args_) const  ;
                };

                 class FN__2038  {
                public:

                  var invoke (ref _args_) const  ;
                };

                 class FN__2039  {
                public:

                  var invoke (ref _args_) const  ;
                };

                 class FN__2040  {
                public:

                  var invoke (ref _args_) const  ;
                };
        }

        // Command Line Arguments
        #if defined(FERRET_STD_LIB) &&               \
            !defined(FERRET_DISABLE_CLI_ARGS) &&     \
            !defined(FERRET_DISABLE_STD_MAIN)
          ferret::var _star_command_line_args_star_;
        #endif

        // Lambda Implementations
        namespace c_interop{
                inline var FN__2037::invoke (ref _args_) const {
                  (void)(_args_);
                  ref a_0 = rt::first(_args_);
                  ref a_1 = rt::first(rt::rest(_args_));
                  ref a_2 = rt::first(rt::rest(rt::rest(_args_)));
                  ref a_3 = rt::first(rt::rest(rt::rest(rt::rest(_args_))));
                  ref a_4 = rt::first(rt::rest(rt::rest(rt::rest(rt::rest(_args_)))));
                  ref a_5 = rt::first(rt::rest(rt::rest(rt::rest(rt::rest(rt::rest(_args_))))));
             
                  var __result;
                  __result = obj<pointer>(new TCanvas(string::to<std::string>(a_0).c_str(), string::to<std::string>(a_1).c_str(), number::to<std::int32_t>(a_2), number::to<std::int32_t>(a_3), number::to<std::int32_t>(a_4), number::to<std::int32_t>(a_5)));
                  return __result;
                }
               

                inline var FN__2038::invoke (ref _args_) const {
                  (void)(_args_);
                  ref a_0 = rt::first(_args_);
                  ref a_1 = rt::first(rt::rest(_args_));
                  ref a_2 = rt::first(rt::rest(rt::rest(_args_)));
                  ref a_3 = rt::first(rt::rest(rt::rest(rt::rest(_args_))));
             
                  var __result;
                  __result = obj<pointer>(new TF1(string::to<std::string>(a_0).c_str(), string::to<std::string>(a_1).c_str(), number::to<std::int32_t>(a_2), number::to<std::int32_t>(a_3)));
                  return __result;
                }
               

                inline var FN__2039::invoke (ref _args_) const {
                  (void)(_args_);
                  ref a_0 = rt::first(_args_);
             
                  pointer::to_pointer<TF1>(a_0)->Draw();
                  return nil();
                }
               

                inline var FN__2040::invoke (ref _args_) const {
                  (void)(_args_);
                  ref a_0 = rt::first(_args_);
                  ref a_1 = rt::first(rt::rest(_args_));
             
                  pointer::to_pointer<TCanvas>(a_0)->Print(string::to<std::string>(a_1).c_str());
                  return nil();
                }
               
        }

        // Program Run
        namespace c_interop{
         void main(){
          nil();
          nil();
          nil();
          (pc1 = run(FN__2037(),obj<string>("c1",2),obj<string>("Something",9),obj<number>(0.0),obj<number>(0.0),obj<number>(800.0),obj<number>(600.0)));
          (pf1 = run(FN__2038(),obj<string>("f1",2),obj<string>("cos(x)",6),obj<number>(-5.0),obj<number>(5.0)));
          run(FN__2039(),pf1);
          run(FN__2040(),pc1,obj<string>("c_interop_1.pdf",15)); 
         }
        }


        #if !defined(FERRET_DISABLE_STD_MAIN)
         #if defined(FERRET_DISABLE_CLI_ARGS) || !defined(FERRET_STD_LIB)
          int main()
         #else
          int main(int argc, char* argv[])
         #endif
          {     
            using namespace ferret;
            FERRET_ALLOCATOR::init();
            rt::init();

           #if defined(FERRET_STD_LIB) && !defined(FERRET_DISABLE_CLI_ARGS)
            for (int i = argc - 1; i > -1 ; i--)
              _star_command_line_args_star_ =  rt::cons(obj<string>(argv[i]),_star_command_line_args_star_);
           #endif

            c_interop::main();

           #if defined(FERRET_PROGRAM_MAIN)
            run(FERRET_PROGRAM_MAIN);
           #endif
             
            return 0;
          }
        #endif
        #if defined(FERRET_HARDWARE_ARDUINO)
          void setup(){
            using namespace ferret;
            FERRET_ALLOCATOR::init();
            rt::init();

            #if defined(FERRET_PROGRAM_MAIN)
              c_interop::main();
            #endif
          }

          void loop(){
            using namespace ferret;
            #if !defined(FERRET_PROGRAM_MAIN)
              c_interop::main();
            #endif          

            #if defined(FERRET_PROGRAM_MAIN)
              run(FERRET_PROGRAM_MAIN);
            #endif
          }
        #endif