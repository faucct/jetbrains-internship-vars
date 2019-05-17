# Vars
[![Build Status](https://travis-ci.org/faucct/jetbrains-internship-vars.svg?branch=master)](https://travis-ci.org/faucct/jetbrains-internship-vars)

Выводит значения всех объявленных в переданных файлах переменных или ошибку, если что-то пошло не так.

## Примеры файлов:

file1.vars:
```
a = 10
b = 15
c = a
```

Кроме декларации переменных есть поддержка импорта из других файлов, в этом случае можно будет обращаться к переменным, заданным в импортированном файле:

file2.vars
```
import file1
d = 40
e = a
foo = c
```

как вы можете заметить в file2.vars использовались переменные a и b из файла file1.vars.

Файл file2.vars можно импортировать в другом файле итд:

file3.vars:
```
import file2
a3 = e
b3 = d
foo3 = foo
```

Импорты не являются транзитивными.

В файле может быть больше одного импорта.
