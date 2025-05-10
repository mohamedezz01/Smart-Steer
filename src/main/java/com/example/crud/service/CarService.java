package com.example.crud.service;

import com.example.crud.dao.CarRepository;
import com.example.crud.entity.Car;
import org.springframework.stereotype.Service;

@Service
public class CarService {

    private final CarRepository carRepository;

    public CarService(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    public Car saveMessage(String right, String left) {
        Car car = new Car();
        car.setRight(right);
        car.setLeft(left);
        return carRepository.save(car);
    }


}
