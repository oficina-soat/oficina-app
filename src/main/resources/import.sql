INSERT INTO public.pessoa (id, documento, tipo_pessoa, nome, email) VALUES
    (1, '84191404067', 'FISICA', 'Administrador Laboratorio', 'admin@oficina.com'),
    (2, '36655462007', 'FISICA', 'Mecanico Laboratorio', 'mecanico@oficina.com'),
    (3, '17245011010', 'FISICA', 'Recepcionista Laboratorio', 'recepcao@oficina.com'),
    (4, '50132372037', 'FISICA', 'Cliente Laboratorio 1', 'cliente1@oficina.com'),
    (5, '12345678900', 'FISICA', 'Cliente Laboratorio 2', 'cliente2@oficina.com');
SELECT setval('pessoa_seq', (SELECT MAX(id) FROM public.pessoa));

INSERT INTO public.papel (id, nome) VALUES
    (1, 'administrativo'),
    (2, 'mecanico'),
    (3, 'recepcionista');
SELECT setval('papel_seq', (SELECT MAX(id) FROM public.papel));

INSERT INTO public.usuario (id, pessoa_id, password, status) VALUES
    (1, 1, '$2a$12$1CBAHD.wKOCpNFGnEMUfn.sMSf8Muag0NWrtrBBxJpssTdZ1OCN3e', 'ATIVO'),
    (2, 2, '$2a$12$1CBAHD.wKOCpNFGnEMUfn.sMSf8Muag0NWrtrBBxJpssTdZ1OCN3e', 'ATIVO'),
    (3, 3, '$2a$12$1CBAHD.wKOCpNFGnEMUfn.sMSf8Muag0NWrtrBBxJpssTdZ1OCN3e', 'ATIVO');
SELECT setval('usuario_seq', (SELECT MAX(id) FROM public.usuario));

INSERT INTO public.usuario_papel (usuario_id, papel_id) VALUES
    (1, 1),
    (1, 2),
    (1, 3),
    (2, 2),
    (3, 3);

INSERT INTO public.cliente (id, pessoa_id, documento, email) VALUES
    (1, 4, '50132372037', 'cliente1@oficina.com'),
    (2, 5, '12345678900', 'cliente2@oficina.com');
SELECT setval('cliente_seq', (SELECT MAX(id) FROM public.cliente));

INSERT INTO public.veiculo (id, placa, marca, modelo, ano) VALUES
    (1, 'ABC1234', '11111111111', '11111111111', 11111111),
    (2, 'ABC1D23', '11111111111', '11111111111', 11111111);
SELECT setval('veiculo_seq', (SELECT MAX(id) FROM public.veiculo));

INSERT INTO public.ordem_de_servico (id, cliente_id, veiculo_id, criado_em, atualizado_em) VALUES
    ('2b2276e8-fa72-4f4c-a3b0-2c5b1bf427ef', 1, 1, '2025-12-14 17:28:14.046297 +00:00', '2025-12-14 17:28:14.046297 +00:00'),
    ('1b2276e8-fa72-4f4c-a3b0-2c5b1bf427ef', 1, 1, '2025-10-14 17:20:14.046297 +00:00', '2025-12-14 17:20:14.046297 +00:00'),
    ('f05dd17b-daae-4658-af7c-363dd6e6fdfb', 1, 1, '2025-12-14 17:28:14.714212 +00:00', '2025-12-14 17:28:14.714212 +00:00'),
    ('5b2276e8-fa72-4f4c-a3b0-2c5b1bf427ef', 1, 1, '2024-12-14 17:28:14.714212 +00:00', '2025-12-14 17:28:14.714212 +00:00'),
    ('4b2276e8-fa72-4f4c-a3b0-2c5b1bf427ef', 1, 1, '2025-12-14 17:28:14.714212 +00:00', '2025-12-14 17:28:14.714212 +00:00'),
    ('6b2276e8-fa72-4f4c-a3b0-2c5b1bf427ef', 1, 1, '2025-12-14 17:28:14.714212 +00:00', '2025-12-14 17:28:14.714212 +00:00'),
    ('7b2276e8-fa72-4f4c-a3b0-2c5b1bf427ef', 1, 1, '2025-12-14 17:28:14.714212 +00:00', '2025-12-14 17:28:14.714212 +00:00'),
    ('4298695b-d6ae-45ac-a659-c4de90f81eb4', 2, 2, '2026-01-17 10:00:00.000000 +00:00', '2026-01-17 10:00:00.000000 +00:00');

INSERT INTO public.estado_ordem_servico (id, tipo_estado, data_estado, ordem_de_servico_id) VALUES
    (1, 'EM_DIAGNOSTICO', '2025-12-14 17:28:14.046297 +00:00', '2b2276e8-fa72-4f4c-a3b0-2c5b1bf427ef'),
    (2, 'EM_DIAGNOSTICO', '2025-12-14 17:20:14.046297 +00:00', '1b2276e8-fa72-4f4c-a3b0-2c5b1bf427ef'),
    (3, 'RECEBIDA', '2025-12-14 17:28:14.714212 +00:00', 'f05dd17b-daae-4658-af7c-363dd6e6fdfb'),
    (4, 'AGUARDANDO_APROVACAO', '2025-12-14 17:28:14.714212 +00:00', '5b2276e8-fa72-4f4c-a3b0-2c5b1bf427ef'),
    (5, 'AGUARDANDO_APROVACAO', '2025-12-14 17:28:14.714212 +00:00', '4b2276e8-fa72-4f4c-a3b0-2c5b1bf427ef'),
    (6, 'EM_EXECUCAO', '2025-12-14 17:28:14.714212 +00:00', '6b2276e8-fa72-4f4c-a3b0-2c5b1bf427ef'),
    (7, 'FINALIZADA', '2025-12-14 17:28:14.714212 +00:00', '7b2276e8-fa72-4f4c-a3b0-2c5b1bf427ef'),
    (8, 'RECEBIDA', '2026-01-17 10:00:00.000000 +00:00', '4298695b-d6ae-45ac-a659-c4de90f81eb4');
SELECT setval('estado_ordem_servico_seq', (SELECT MAX(id) FROM public.estado_ordem_servico));

INSERT INTO public.peca (id, nome) VALUES
    (1, 'Volante'),
    (2, 'Pneu'),
    (3, 'Tapete');
SELECT setval('peca_seq', (SELECT MAX(id) FROM public.peca));

INSERT INTO public.os_item_peca (quantidade, valor_total, valor_unitario, id, peca_id, ordem_de_servico_id) VALUES
    (2.000, 2.00, 1.00, 1, 1, '2b2276e8-fa72-4f4c-a3b0-2c5b1bf427ef'),
    (2.000, 100.00, 50.00, 2, 1, '4298695b-d6ae-45ac-a659-c4de90f81eb4');
SELECT setval('os_item_peca_seq', (SELECT MAX(id) FROM public.os_item_peca));

INSERT INTO public.estoque_saldo (quantidade, peca_id) VALUES (50.000, 1);

INSERT INTO public.servico (id, nome) VALUES (1, 'Troca de oleo');
SELECT setval('servico_seq', (SELECT MAX(id) FROM public.servico));

INSERT INTO public.os_item_servico (quantidade, valor_total, valor_unitario, id, servico_id, ordem_de_servico_id) VALUES
    (1.000, 120.00, 150.00, 1, 1, '4298695b-d6ae-45ac-a659-c4de90f81eb4');
SELECT setval('os_item_servico_seq', (SELECT MAX(id) FROM public.os_item_servico));
